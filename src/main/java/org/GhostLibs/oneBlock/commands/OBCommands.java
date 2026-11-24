package org.GhostLibs.oneBlock.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import org.GhostLibs.oneBlock.Sprache;
import org.GhostLibs.oneBlock.gui.IslandListGUI;
import org.GhostLibs.oneBlock.manager.OBManager;
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
        IslandListGUI gui = new IslandListGUI(player.getUniqueId());
        gui.CreateGUI();
        player.openInventory(gui.getInventory());
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
}