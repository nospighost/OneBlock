package org.GhostLibs.oneBlock.listener;

import org.GhostLibs.oneBlock.events.OBCreatePlayerData;
import org.GhostLibs.oneBlock.manager.OBManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;

public class JoinListener implements Listener {

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

}
