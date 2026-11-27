package org.GhostLibs.oneBlock.manager;

import org.GhostLibs.oneBlock.model.IslandPhase;
import org.GhostLibs.oneBlock.model.PlayerIsland;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class IslandCache {

    public static final class Entry {
        private final PlayerIsland island;
        private final IslandPhase phase;
        private volatile boolean dirty;

        private Entry(PlayerIsland island, IslandPhase phase) {
            this.island = island;
            this.phase = phase;
        }

        public PlayerIsland island() {
            return island;
        }

        public IslandPhase phase() {
            return phase;
        }

        public void markDirty() {
            this.dirty = true;
        }

    }

    private final OBManager manager;
    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();
    private final Set<UUID> loading = ConcurrentHashMap.newKeySet();

    public IslandCache(OBManager manager) {
        this.manager = manager;
    }

    public static UUID ownerOf(World world) {
        if (world == null) return null;
        String name = world.getName();
        if (!name.startsWith(WorldManager.obWorldPrefix)) return null;
        try {
            return UUID.fromString(name.substring(WorldManager.obWorldPrefix.length()));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void start(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, this::saveAll, 1200L, 1200L);
    }

    public Entry get(UUID owner) {
        return entries.get(owner);
    }

    public CompletableFuture<Entry> load(UUID owner) {
        Entry cached = entries.get(owner);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        if (!loading.add(owner)) {
            return CompletableFuture.completedFuture(null);
        }

        return manager.getPlayerIsland(owner)
                .thenCompose(island -> {
                    if (island == null) {
                        return CompletableFuture.<Entry>completedFuture(null);
                    }
                    return manager.getIslandPhase(island.getIslandId()).thenCompose(phase -> {
                        if (phase == null) {
                            return CompletableFuture.<Entry>completedFuture(null);
                        }
                        return manager.getIslandMembers(island.getIslandId()).thenApply(members -> {
                            island.setMembers(members);
                            Entry entry = new Entry(island, phase);
                            entries.put(owner, entry);
                            return entry;
                        });
                    });
                })
                .whenComplete((entry, ex) -> loading.remove(owner));
    }

    public CompletableFuture<Void> reloadMembers(UUID owner) {
        Entry entry = entries.get(owner);
        if (entry == null) {
            return CompletableFuture.completedFuture(null);
        }
        return manager.getIslandMembers(entry.island().getIslandId())
                .thenAccept(members -> entry.island().setMembers(members));
    }

    public void remove(UUID owner) {
        entries.remove(owner);
    }

    public CompletableFuture<Void> saveAndRemove(UUID owner) {
        Entry entry = entries.remove(owner);
        if (entry == null) {
            return CompletableFuture.completedFuture(null);
        }
        return persist(entry);
    }

    public CompletableFuture<Void> saveAll() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Entry entry : entries.values()) {
            futures.add(persist(entry));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> persist(Entry entry) {
        if (!entry.dirty) {
            return CompletableFuture.completedFuture(null);
        }

        entry.dirty = false;

        return manager.updateIslandPhase(entry.phase).handle((success, ex) -> {
            if (ex != null || !Boolean.TRUE.equals(success)) {
                entry.dirty = true;
            }
            return (Void) null;
        });
    }
}