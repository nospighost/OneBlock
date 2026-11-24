package org.GhostLibs.oneBlock.listener;

import org.GhostLibs.oneBlock.OneBlock;
import org.GhostLibs.oneBlock.events.OBCreatePlayerData;
import org.GhostLibs.oneBlock.manager.OBManager;
import org.GhostLibs.oneBlock.manager.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class JoinListener implements Listener {

    private WorldManager worldManager;

    public JoinListener(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {

        OBCreatePlayerData OBEvent = new OBCreatePlayerData(event.getPlayer());
        Bukkit.getPluginManager().callEvent(OBEvent);

        if (OBEvent.isCancelled()) return;

        Map<String, Object> data = new HashMap<>();
        data.put("uuid", event.getPlayer().getUniqueId().toString());
        data.put("island_resets", 0);
        data.put("last_seen", System.currentTimeMillis());
        data.put("auto_teleport_island", false);

        OBManager.getInstance().createPlayerData(data);
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!worldManager.hasWorld(event.getPlayer().getUniqueId())) return;
        World world = worldManager.getWorld(event.getPlayer().getUniqueId());
        worldManager.unloadIfEmpty(world);
    }


}
