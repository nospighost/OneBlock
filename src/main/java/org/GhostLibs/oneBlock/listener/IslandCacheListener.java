package org.GhostLibs.oneBlock.listener;

import org.GhostLibs.oneBlock.manager.IslandCache;
import org.GhostLibs.oneBlock.manager.WorldManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.UUID;

public class IslandCacheListener implements Listener {

    private final WorldManager worldManager;
    private final IslandCache cache;

    public IslandCacheListener(IslandCache cache, WorldManager worldManager) {
        this.cache = cache;
        this.worldManager = worldManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        preload(event.getPlayer());
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        preload(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!worldManager.hasWorld(event.getPlayer().getUniqueId())) return;
        World world = worldManager.getWorld(event.getPlayer().getUniqueId());
        worldManager.unloadIfEmpty(world);
        cache.saveAndRemove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        UUID owner = IslandCache.ownerOf(event.getWorld());
        if (owner != null) {
            cache.saveAndRemove(owner);
        }
    }

    private void preload(Player player) {
        UUID owner = IslandCache.ownerOf(player.getWorld());
        if (owner != null) {
            cache.load(owner);
        }
    }
}