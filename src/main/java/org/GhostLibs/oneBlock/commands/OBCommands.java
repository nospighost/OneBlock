package org.GhostLibs.oneBlock.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import org.GhostLibs.oneBlock.Sprache;
import org.GhostLibs.oneBlock.gui.IslandListGUI;
import org.GhostLibs.oneBlock.gui.MemberPermissionGUI;
import org.GhostLibs.oneBlock.manager.OBManager;
import org.GhostLibs.oneBlock.model.MemberResult;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@CommandAlias("ob")
public class OBCommands extends BaseCommand {

    private final JavaPlugin plugin;

    public OBCommands(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandPermission("oneblock.default")
    public void onDefault(Player player) {
        IslandListGUI.open(player);
    }

    @Subcommand("create")
    @CommandPermission("oneblock.create")
    public void onCreate(Player player) {
        OBManager manager = OBManager.getInstance();
        manager.sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_CREATING"));

        manager.getPlayerID(player.getUniqueId())
                .thenCompose(playerId -> {
                    if (playerId <= 0) {
                        manager.sendMessageSync(player, Sprache.get("ERRORS.NO_PLAYER_DATA", "%player%", player.getName()));
                        return CompletableFuture.<Void>completedFuture(null);
                    }

                    return manager.getPlayerData(playerId)
                            .thenCompose(playerData -> manager.createIsland(player, playerId, playerData));
                })
                .exceptionally(exception -> {
                    plugin.getLogger().log(Level.SEVERE, "Island creation failed.", exception);
                    manager.sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_ERROR_CREATED"));
                    return null;
                });
    }


    @Subcommand("delete")
    @CommandPermission("oneblock.delete")
    public void onDelete(Player player) {
        OBManager manager = OBManager.getInstance();

        manager.deleteIsland(player.getUniqueId())
                .thenAccept(id -> {
                    if (id == -3) {
                        manager.sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_NO_ISLAND", "%player%", player.getName()));
                    } else if (id == -1) {
                        manager.sendMessageSync(player, Sprache.get("COMMANDS.NO_PLAYER_DATA", "%player%", player.getName()));
                    } else if (id == -2) {
                        manager.sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_DELETE_ERROR", "%player%", player.getName()));
                    } else {
                        manager.sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_DELETE", "%player%", player.getName()));
                    }
                })
                .exceptionally(exception -> {
                    plugin.getLogger().log(Level.SEVERE, "Island deletion failed.", exception);
                    manager.sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_DELETE_ERROR"));
                    return null;
                });
    }

    @Subcommand("add")
    @CommandPermission("oneblock.add")
    @CommandCompletion("@players")
    @Syntax("<Spieler>")
    public void onAdd(Player player, String targetName) {
        OBManager manager = OBManager.getInstance();

        OfflinePlayer target = findPlayer(targetName);
        if (target == null) {
            manager.sendMessageSync(player, Sprache.get("COMMANDS.MEMBER_PLAYER_NOT_FOUND", "%player%", targetName));
            return;
        }

        manager.addIslandMember(player.getUniqueId(), target.getUniqueId())
                .thenAccept(result -> manager.sendMessageSync(player, memberMessage(result, targetName)))
                .exceptionally(exception -> {
                    plugin.getLogger().log(Level.SEVERE, "Adding island member failed.", exception);
                    manager.sendMessageSync(player, Sprache.get("COMMANDS.MEMBER_ERROR"));
                    return null;
                });
    }

    @Subcommand("remove")
    @CommandPermission("oneblock.remove")
    @CommandCompletion("@players")
    @Syntax("<Spieler>")
    public void onRemove(Player player, String targetName) {
        OBManager manager = OBManager.getInstance();

        OfflinePlayer target = findPlayer(targetName);
        if (target == null) {
            manager.sendMessageSync(player, Sprache.get("COMMANDS.MEMBER_PLAYER_NOT_FOUND", "%player%", targetName));
            return;
        }

        manager.removeIslandMember(player.getUniqueId(), target.getUniqueId())
                .thenAccept(result -> manager.sendMessageSync(player, memberMessage(result, targetName)))
                .exceptionally(exception -> {
                    plugin.getLogger().log(Level.SEVERE, "Removing island member failed.", exception);
                    manager.sendMessageSync(player, Sprache.get("COMMANDS.MEMBER_ERROR"));
                    return null;
                });
    }

    @Subcommand("permissions|perms")
    @CommandPermission("oneblock.permissions")
    @CommandCompletion("@players")
    @Syntax("<Spieler>")
    public void onPermissions(Player player, String targetName) {
        OBManager manager = OBManager.getInstance();

        OfflinePlayer target = findPlayer(targetName);
        if (target == null) {
            manager.sendMessageSync(player, Sprache.get("COMMANDS.MEMBER_PLAYER_NOT_FOUND", "%player%", targetName));
            return;
        }

        manager.getIslandMember(player.getUniqueId(), target.getUniqueId())
                .thenAccept(member -> {
                    if (member == null) {
                        manager.sendMessageSync(player, Sprache.get("COMMANDS.MEMBER_NOT_MEMBER", "%player%", targetName));
                        return;
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!player.isOnline()) return;
                        MemberPermissionGUI.open(player, player.getUniqueId(), member);
                    });
                })
                .exceptionally(exception -> {
                    plugin.getLogger().log(Level.SEVERE, "Opening member permissions failed.", exception);
                    manager.sendMessageSync(player, Sprache.get("COMMANDS.MEMBER_ERROR"));
                    return null;
                });
    }

    private OfflinePlayer findPlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }
        return Bukkit.getOfflinePlayerIfCached(name);
    }

    private String memberMessage(MemberResult result, String targetName) {
        return switch (result) {
            case ADDED -> Sprache.get("COMMANDS.MEMBER_ADDED", "%player%", targetName);
            case REMOVED -> Sprache.get("COMMANDS.MEMBER_REMOVED", "%player%", targetName);
            case NO_ISLAND -> Sprache.get("COMMANDS.MEMBER_NO_ISLAND");
            case NO_PLAYER_DATA -> Sprache.get("COMMANDS.MEMBER_NO_PLAYER_DATA", "%player%", targetName);
            case ALREADY_MEMBER -> Sprache.get("COMMANDS.MEMBER_ALREADY", "%player%", targetName);
            case NOT_MEMBER -> Sprache.get("COMMANDS.MEMBER_NOT_MEMBER", "%player%", targetName);
            case IS_OWNER -> Sprache.get("COMMANDS.MEMBER_IS_OWNER");
            case ERROR -> Sprache.get("COMMANDS.MEMBER_ERROR");
        };
    }
}
