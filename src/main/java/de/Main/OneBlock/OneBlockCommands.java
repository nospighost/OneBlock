package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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

        Player player = (Player) sender;
        YamlConfiguration config = Manager.getIslandConfig(player);

        if (args.length == 1 && args[0].equalsIgnoreCase("join")) {
            player.sendMessage("§a Insel wird erstellt bitte habe Geduld");
            Manager.createOrJoinIsland(player, args);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("delete")) {
            Manager.deleteIsland(player);
        }else if (args.length == 2 && args[0].equalsIgnoreCase("visit")) {
                String targetName = args[1];
                Manager.visitIsland(player, targetName);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("rebirth")) {

            if (config.getInt("IslandLevel") != 4) {  //a

                player.sendMessage("§cDein Inventar ist voll!");

            }else if (config.getInt("IslandLevel") == 4 || (player.getInventory().firstEmpty() == -1)){
                Manager.rebirthIsland(player);



            }

            }
         else {
                player.sendMessage("Nutze: /ob join | /ob delete");
            }


            return true;
    }
}
