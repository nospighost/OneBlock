package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

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
            player.sendMessage(prefix + Main.config.getString("islandjoinmessage.create"));
            Manager.createOrJoinIsland(player, args);

        } else if (args.length == 1 && args[0].equalsIgnoreCase("delete")) {
            YamlConfiguration config = Manager.getIslandConfig(player);
            if (!isOwnerOfIsland(player, config)) {
                player.sendMessage(prefix + "§cNur der Inselbesitzer kann die Insel löschen.");
                return true;
            }
            Manager.deleteIsland(player);

        } else if (args.length == 2 && args[0].equalsIgnoreCase("visit")) {
            String targetName = args[1];
            Manager.visitIsland(player, targetName);

        } else if (args.length == 1 && args[0].equalsIgnoreCase("rebirth")) {
            YamlConfiguration config = Manager.getIslandConfig(player);
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
            YamlConfiguration config = Manager.getIslandConfig(player);
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
            Manager.acceptInvite(player);

        } else if (args.length == 2 && args[0].equalsIgnoreCase("deny")) {
                YamlConfiguration config = Manager.getIslandConfig(player);
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
            YamlConfiguration config = Manager.getIslandConfig(player);
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
            YamlConfiguration config = Manager.getIslandConfig(player);
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
            String ownerName = args[1];
            Manager.leaveIsland(player, ownerName);

        } else {
            player.sendMessage(prefix + "§aNutze: /ob join | /ob delete | /ob visit <Spieler> | /ob rebirth | /ob add <Spieler> | /ob trust <Spieler> | /ob accept | /ob deny <Spieler> | /ob unban <Spieler> | /ob remove <Spieler> | /ob leave <Inselbesitzer>");
        }

        return true;
    }

    private boolean isOwnerOfIsland(Player player, YamlConfiguration islandConfig) {
        if (islandConfig == null) return false;
        String ownerName = islandConfig.getString("owner");
        return ownerName != null && ownerName.equalsIgnoreCase(player.getName());
    }
}
