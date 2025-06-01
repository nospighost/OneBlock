package de.Main.OneBlock.OneBlock.Commands;

import de.Main.OneBlock.Main;

import de.Main.OneBlock.OneBlock.Manager.Manager;
import de.Main.OneBlock.database.DBM;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
            Manager.createOrJoinIsland(player, args);

        } else if (args.length == 1 && args[0].equalsIgnoreCase("delete")) {
            if (!hasPermissionOrOp(player, "oneblock.delete")) {
                player.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }
            if (!isOwnerOfIsland(player)) {
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

            if (!isOwnerOfIsland(player)) {
                player.sendMessage(prefix + "§cNur der Inselbesitzer kann rebirth ausführen.");
                return true;
            }

            int currentLevel = DBM.getInt("userdata",player.getUniqueId(), "IslandLevel", 0);
            int rebirthLevel = Main.config.getInt("RebirthLevel");

            if (currentLevel != rebirthLevel) {
                String message = Main.config.getString("rebirthhigherlevel", "§cDein Insel-Level " + "muss %level% erreichen, um Rebirth auszuführen.").replace("%level%", String.valueOf(rebirthLevel));
                player.sendMessage(prefix + message);
                return true;
            }

            if (player.getInventory().firstEmpty() == -1) {
                String fullInventoryMsg = Main.config.getString("rebirthinventoryfull", "§cDein Inventar ist voll. Bitte räume Platz, bevor du Rebirth ausführst.");
                player.sendMessage(prefix + fullInventoryMsg);
                return true;
            }

            Manager.rebirthIsland(player);
            return true;

        } else if (args.length == 2 && args[0].equalsIgnoreCase("trust")) {
            if (!hasPermissionOrOp(player, "oneblock.trust")) {
                player.sendMessage("§cDazu hast du keine Berechtigung.");
                return true;
            }
            if (!isOwnerOfIsland(player)) {
                player.sendMessage(prefix + "§cNur der Inselbesitzer kann Spielern vertrauen.");
                return true;
            }
            OfflinePlayer toTrust = Bukkit.getOfflinePlayer(args[1]);
            if (toTrust != null && (toTrust.isOnline() || toTrust.hasPlayedBefore())) {
                Manager.trustPlayer(player, toTrust.getPlayer() != null ? toTrust.getPlayer() : player);
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
            if (!isOwnerOfIsland(player)) {
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
            if (!isOwnerOfIsland(player)) {
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
            if (!isOwnerOfIsland(player)) {
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
            if (target == null || target.isEmpty()) {
                player.sendMessage("§cBitte gib einen gültigen Spieler an.");
                return true;
            }
            Manager.declineInvite(player, target);

        } else {
            player.sendMessage(prefix + "§aNutze:");
            player.sendMessage("§7/ob join - Insel erstellen oder betreten");
            player.sendMessage("§7/ob delete - Insel löschen");
            player.sendMessage("§7/ob visit <Spieler> - Insel eines Spielers besuchen");
            player.sendMessage("§7/ob rebirth - Insel auf Rebirth zurücksetzen");
            player.sendMessage("§7/ob trust <Spieler> - Spieler Vertrauen schenken");
            player.sendMessage("§7/ob accept - Einladung annehmen");
            player.sendMessage("§7/ob deny <Spieler> - Einladung ablehnen");
            player.sendMessage("§7/ob unban <Spieler> - Spieler entbannen");
            player.sendMessage("§7/ob remove <Spieler> - Spieler von der Insel entfernen");
            player.sendMessage("§7/ob leave <Inselbesitzer> - Insel verlassen");
        }

        return true;
    }

    private boolean isOwnerOfIsland(Player player) {
        UUID playerUUID = player.getUniqueId();
        boolean ownsIsland = DBM.getBoolean("userdata",playerUUID, "EigeneInsel", false);
        UUID ownerUUID;
        try {
            ownerUUID = DBM.getUUID("userdata",playerUUID, "owner_uuid", playerUUID);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Fehler beim Abrufen der Besitzer-UUID: " + e.getMessage());
            return false;
        }
        return ownsIsland && playerUUID.equals(ownerUUID);
    }


    private boolean hasPermissionOrOp(Player player, String permission) {
        return player.hasPermission("oneblock.admin") || player.hasPermission(permission) || player.isOp();
    }
}
