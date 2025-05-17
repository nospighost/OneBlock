package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

public class OneBlockCommands implements Listener, CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl benutzen.");
            return true;
        }

        String prefix = Main.config.getString("Server");
        Player player = (Player) sender;

        if (args.length == 1 && args[0].equalsIgnoreCase("join")) {
            if (!hasPermissionOrOp(player, "oneblock.join")) {
                player.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }
            player.sendMessage(prefix + Main.config.getString("islandjoinmessage.create"));
            Manager.createOrJoinIsland(player, args);

        } else if (args.length == 1 && args[0].equalsIgnoreCase("delete")) {
            if (!hasPermissionOrOp(player, "oneblock.delete")) {
                player.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }
            YamlConfiguration config = Manager.getIslandConfig(player.getUniqueId());
            if (!isOwnerOfIsland(player, config)) {
                player.sendMessage(prefix + "§cNur der Inselbesitzer kann die Insel löschen.");
                return true;
            }
            Manager.deleteIsland(player);

        } else if (args.length == 2 && args[0].equalsIgnoreCase("visit")) {
            if (!hasPermissionOrOp(player, "oneblock.visit")) {
                player.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }
            String targetName = args[1];
            Manager.visitIsland(player, targetName);

        } else if (args.length == 1 && args[0].equalsIgnoreCase("rebirth")) {
            if (!hasPermissionOrOp(player, "oneblock.rebirth")) {
                player.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }
            YamlConfiguration config = Manager.getIslandConfig(player.getUniqueId());
            if (!isOwnerOfIsland(player, config)) {
                player.sendMessage(prefix + "§cNur der Inselbesitzer kann rebirth ausführen.");
                return true;
            }
            if (config.getInt("IslandLevel") != Main.config.getInt("RebirthLevel")) {
                player.sendMessage(prefix + Main.config.getString("rebirthhigherlevel").replace("%level%", Main.config.getInt("RebirthLevel") + ""));
            } else if ((player.getInventory().firstEmpty() == -1)) {
                player.sendMessage(prefix + Main.config.getString("rebirthinventoryfull"));
            } else {
                Manager.rebirthIsland(player);
            }

        } else if (args.length == 2 && args[0].equalsIgnoreCase("trust")) {
            if (!hasPermissionOrOp(player, "oneblock.trust")) {
                player.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }
            YamlConfiguration config = Manager.getIslandConfig(player.getUniqueId());
            if (!isOwnerOfIsland(player, config)) {
                player.sendMessage(prefix + "§cNur der Inselbesitzer kann Spielern vertrauen.");
                return true;
            }
            OfflinePlayer toTrust = Bukkit.getOfflinePlayer(args[1]);
            if (toTrust != null && (toTrust.isOnline() || toTrust.hasPlayedBefore())) {
                Manager.trustPlayer(player, toTrust.getPlayer() != null ? toTrust.getPlayer() : player); // fallback
            } else {
                player.sendMessage(prefix + "§cSpieler nicht gefunden.");
            }

        } else if (args.length == 1 && args[0].equalsIgnoreCase("accept")) {
            if (!hasPermissionOrOp(player, "oneblock.accept")) {
                player.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }
            Manager.acceptInvite(player);

        } else if (args.length == 2 && args[0].equalsIgnoreCase("deny")) {
            if (!hasPermissionOrOp(player, "oneblock.deny")) {
                player.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }
            YamlConfiguration config = Manager.getIslandConfig(player.getUniqueId());
            if (!isOwnerOfIsland(player, config)) {
                player.sendMessage(prefix + "§cNur der Inselbesitzer kann Spieler bannen.");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (target != null && target.getName() != null && (target.hasPlayedBefore() || target.isOnline())) {
                Manager.denyfromisland(player, target);
            } else {
                player.sendMessage(prefix + "§cSpieler nicht gefunden.");
            }

        } else if (args.length == 2 && args[0].equalsIgnoreCase("unban")) {
            if (!hasPermissionOrOp(player, "oneblock.unban")) {
                player.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }
            YamlConfiguration config = Manager.getIslandConfig(player.getUniqueId());
            if (!isOwnerOfIsland(player, config)) {
                player.sendMessage(prefix + "§cNur der Inselbesitzer kann Spieler entbannen.");
                return true;
            }
            OfflinePlayer toUnban = Bukkit.getOfflinePlayer(args[1]);
            if (toUnban != null && (toUnban.isOnline() || toUnban.hasPlayedBefore())) {
                Manager.unban(player, toUnban.getPlayer() != null ? toUnban.getPlayer() : player);
            } else {
                player.sendMessage(prefix + "§cSpieler nicht gefunden.");
            }

        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            if (!hasPermissionOrOp(player, "oneblock.remove")) {
                player.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }
            YamlConfiguration config = Manager.getIslandConfig(player.getUniqueId());
            if (!isOwnerOfIsland(player, config)) {
                player.sendMessage(prefix + "§cNur der Inselbesitzer kann Spieler entfernen.");
                return true;
            }
            OfflinePlayer toRemove = Bukkit.getOfflinePlayer(args[1]);
            if (toRemove != null && (toRemove.isOnline() || toRemove.hasPlayedBefore())) {
                Manager.remove(player, toRemove.getPlayer() != null ? toRemove.getPlayer() : player);
            } else {
                player.sendMessage(prefix + "§cSpieler nicht gefunden.");
            }

        } else if (args.length == 2 && args[0].equalsIgnoreCase("leave")) {
            if (!hasPermissionOrOp(player, "oneblock.leave")) {
                player.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }
            String ownerName = args[1];
            Manager.leaveIsland(player, ownerName);

        } else if (args.length == 2 && args[0].equalsIgnoreCase("decline")) {
            if (!hasPermissionOrOp(player, "oneblock.decline")) {
                player.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }
            String target = args[1];
            Manager.declineinvite(player, target);

        } else {
            player.sendMessage(prefix + "§aNutze: /ob join | /ob delete | /ob visit <Spieler> | /ob rebirth | /ob trust <Spieler> | /ob accept | /ob deny <Spieler> | /ob unban <Spieler> | /ob remove <Spieler> | /ob leave <Inselbesitzer>");
        }

        return true;
    }

    private boolean isOwnerOfIsland(Player player, YamlConfiguration islandConfig) {
        if (islandConfig == null) return false;
        String ownerUUIDString = islandConfig.getString("owner-uuid");
        if (ownerUUIDString == null) return false;

        try {
            UUID ownerUUID = UUID.fromString(ownerUUIDString);
            return ownerUUID.equals(player.getUniqueId());
        } catch (IllegalArgumentException e) {
            return ownerUUIDString.equalsIgnoreCase(player.getName());
        }
    }

    private boolean hasPermissionOrOp(Player player, String permission) {
        return player.hasPermission("oneblock.admin") || player.hasPermission(permission) || player.isOp();
    }
}
