package de.Main.OneBlock.OneBlock.Player;

import de.Main.OneBlock.Main;
import de.Main.OneBlock.database.DatenBankManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        int pos1 = DatenBankManager.getInt(event.getPlayer().getUniqueId(), "OneBlock_x", 0);
        int pos2 = DatenBankManager.getInt(event.getPlayer().getUniqueId(), "OneBlock_z", 0);
        World world = Bukkit.getWorld(Main.WORLD_NAME);
        Location tp = new Location(world, pos1, 101, pos2);
        event.setRespawnLocation(tp);

    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (!world.equals(Main.WORLD_NAME)) {
            Location spawnLOC = new Location(Main.oneBlockWorld, 0, 100, 0);
            player.teleport(spawnLOC);
        }
    }


}
