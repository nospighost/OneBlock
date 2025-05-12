package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

        if (args.length == 1 && args[0].equalsIgnoreCase("join")){
            Manager.createOrJoinIsland(player, args);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("delete")){
            OBGUI.openmaingui(player);
        } else {
            player.sendMessage("Nutze: /ob join | /ob delete");
        }



        return true;
    }
}
