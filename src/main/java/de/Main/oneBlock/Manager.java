package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Manager implements Listener {

    // Methode gibt keinen Wert zurück (void)
    public static boolean createOrJoinIsland(Player player, String[] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("join")) {
            World world = Bukkit.getWorld("OneBlock");
            if (world != null) {
                Location spawnLocation = new Location(world, 0, 101, 0);
                player.teleport(spawnLocation);
                player.sendMessage("" + Main.config.getString("islandjoinmessage.join"));
            } else {
                player.sendMessage("§cOneBlock-Welt nicht gefunden!");
            }
            return true;


        }
        player.sendMessage("§cBenutzung: /ob join");
        return false;
    }
}
