package org.GhostLibs.oneBlock.manager;

import org.GhostLibs.oneBlock.OneBlock;
import org.GhostLibs.oneBlock.util.ErrorLogger;
import org.GhostLibs.oneBlock.util.VoidGenerator;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class WorldManager {

    private final JavaPlugin plugin;
    public static final String obWorldPrefix = "ob_";
    private static final int MIN_VIEW_DISTANCE = 2;
    private static final long RESTORE_VIEW_DISTANCE_DELAY_TICKS = 60L;
    private boolean debug = false;

    public WorldManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isOneBlockWorld(World world) {
        return world.getName().startsWith(obWorldPrefix);
    }

    public boolean isIslandOwner(Player player) {
        return worldName(player.getUniqueId()).equals(player.getWorld().getName());
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }

    private void debugMsg(CommandSender player, String message) {
        if (!debug || player == null) return;
        player.sendMessage("§7[Debug] " + message);
    }

    private void debugMsg(OfflinePlayer player, String message) {
        if (!debug || player == null) return;
        Player online = player.getPlayer();
        if (online != null) {
            online.sendMessage("§7[Debug] " + message);
        }
    }

    private void debugMsg(Player player, String message) {
        if (!debug || player == null) return;
        player.sendMessage("§7[Debug] " + message);
    }

    public String worldName(UUID uuid) {
        return obWorldPrefix + uuid;
    }

    public boolean hasWorld(UUID uuid) {
        try {
            File folder = new File(Bukkit.getWorldContainer(), worldName(uuid));
            return folder.exists();
        } catch (Exception e) {
            ErrorLogger.log("Fehler beim Prüfen ob Welt existiert (hasWorld) im WorldManager.java: " + e.getMessage(), e);
            return false;
        }
    }

    public World getWorld(UUID uuid) {
        try {
            return Bukkit.getWorld(worldName(uuid));
        } catch (Exception e) {
            ErrorLogger.log("Fehler beim Abrufen der Welt (getWorld) im WorldManager.java: " + e.getMessage(), e);
            return null;
        }
    }

    public World getWorldByLocation(Location loc) {
        try {
            return loc == null ? null : loc.getWorld();
        } catch (Exception e) {
            ErrorLogger.log("Fehler beim Abrufen der Welt anhand der Location (getWorldByLocation) im WorldManager.java: " + e.getMessage(), e);
            return null;
        }
    }

    public CompletableFuture<World> createWorld(OfflinePlayer player, int borderSize) {
        UUID uuid = player.getUniqueId();
        CompletableFuture<World> result = new CompletableFuture<>();

        CompletableFuture<Void> cleanup = hasWorld(uuid) ? deleteWorld(uuid) : CompletableFuture.completedFuture(null);

        cleanup.whenComplete((v, ex) -> {
            if (ex != null) {
                result.completeExceptionally(ex);
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () ->
                    loadOrCreate(player, uuid, true, borderSize).whenComplete((world, t) -> {
                        if (t != null) {
                            result.completeExceptionally(t);
                        } else {
                            result.complete(world);
                        }
                    }));
        });

        return result;
    }

    public CompletableFuture<World> loadExistingWorld(OfflinePlayer sender, UUID uuid, int borderSize) {
        if (!hasWorld(uuid)) {
            CompletableFuture<World> f = new CompletableFuture<>();
            f.completeExceptionally(new IllegalStateException("Keine Insel gefunden"));
            return f;
        }
        CompletableFuture<World> result = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () ->
                loadOrCreate(sender, uuid, false, borderSize).whenComplete((world, t) -> {
                    if (t != null) {
                        result.completeExceptionally(t);
                    } else {
                        result.complete(world);
                    }
                }));
        return result;
    }

    private CompletableFuture<World> loadOrCreate(OfflinePlayer player, UUID islandUUID, boolean placeSpawnBlock, int borderSize) {
        CompletableFuture<World> future = new CompletableFuture<>();
        String name = worldName(islandUUID);

        final long start = System.nanoTime();
        debugMsg(player, "Starte loadOrCreate() für " + name);

        try {
            World existing = Bukkit.getWorld(name);
            if (existing != null) {
                debugMsg(player, "Welt bereits geladen: " + name);
                debugMsg(player, "Lade Welt in " + ((System.nanoTime() - start) / 1_000_000) + "ms");
                future.complete(existing);
                return future;
            }

            debugMsg(player, "Erstelle neue Welt: " + name);

            WorldCreator creator = new WorldCreator(name);
            creator.generator(new VoidGenerator());
            creator.generateStructures(false);
            creator.environment(World.Environment.NORMAL);
            creator.type(WorldType.FLAT);

            World world = Bukkit.createWorld(creator);
            if (world == null) {
                RuntimeException ex = new RuntimeException("Welt konnte nicht erstellt/geladen werden.");
                ErrorLogger.log("Welt konnte nicht erstellt/geladen werden (loadOrCreate) im WorldManager.java für " + name, ex);
                future.completeExceptionally(ex);
                return future;
            }

            debugMsg(player, "Welt erstellt in " + ((System.nanoTime() - start) / 1_000_000) + "ms");

            if (placeSpawnBlock) {
                world.setViewDistance(MIN_VIEW_DISTANCE);
                world.setSimulationDistance(MIN_VIEW_DISTANCE);
                debugMsg(player, "View-/Simulation-Distance temporär auf " + MIN_VIEW_DISTANCE + " gesetzt.");
            }

            applyPerformanceSettings(world);
            applyWorldBorderSettings(world, borderSize);
            Location blockLoc = new Location(world, 0, 100, 0);
            world.setSpawnLocation(new Location(world, 0, 101, 0));

            world.getChunkAtAsync(blockLoc).thenAccept(chunk -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        debugMsg(player, "Chunk geladen: " + chunk.getX() + "/" + chunk.getZ());

                        if (placeSpawnBlock) {
                            blockLoc.getBlock().setType(Material.GRASS_BLOCK, false);
                            debugMsg(player, "Spawnblock gesetzt");

                            int normalViewDistance = Bukkit.getViewDistance();
                            int normalSimulationDistance = Bukkit.getSimulationDistance();

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                World stillLoaded = Bukkit.getWorld(name);
                                if (stillLoaded == null) return;

                                stillLoaded.setViewDistance(normalViewDistance);
                                stillLoaded.setSimulationDistance(normalSimulationDistance);
                                debugMsg(player, "View-/Simulation-Distance wieder auf Normalwert (" + normalViewDistance + "/" + normalSimulationDistance + ") gesetzt.");
                            }, RESTORE_VIEW_DISTANCE_DELAY_TICKS);
                        }

                        debugMsg(player, "loadOrCreate() abgeschlossen in " +
                                ((System.nanoTime() - start) / 1_000_000) + "ms");

                        future.complete(world);
                    } catch (Exception e) {
                        ErrorLogger.log("Fehler im Chunk-Callback von loadOrCreate() im WorldManager.java für " + name + ": " + e.getMessage(), e);
                        future.completeExceptionally(e);
                    }
                });
            }).exceptionally(ex -> {
                ErrorLogger.log("Fehler beim asynchronen Laden des Chunks in loadOrCreate() im WorldManager.java für " + name + ": " + ex.getMessage(), ex);
                future.completeExceptionally(ex);
                return null;
            });

        } catch (Exception e) {
            ErrorLogger.log("Fehler in loadOrCreate() im WorldManager.java für " + name + ": " + e.getMessage(), e);
            future.completeExceptionally(e);
        }

        return future;
    }

    private void applyPerformanceSettings(World w) {
        try {
            w.setGameRule(GameRules.ADVANCE_WEATHER, false);
            w.setGameRule(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0);
            w.setGameRule(GameRules.ADVANCE_TIME, false);
        } catch (Exception e) {
            ErrorLogger.log("Fehler beim Anwenden der Performance-Einstellungen (applyPerformanceSettings) im WorldManager.java: " + e.getMessage(), e);
        }
    }

    public void applyWorldBorderSettings(World w, int size) {
        try {
            WorldBorder border = w.getWorldBorder();
            border.setCenter(0.5, 0.5);
            border.setSize(size);
        } catch (Exception e) {
            ErrorLogger.log("Fehler beim Anwenden der WorldBorder-Einstellungen (applyWorldBorderSettings) im WorldManager.java: " + e.getMessage(), e);
        }
    }

    public void unloadIfEmpty(World world) {
        try {
            if (world == null) return;
            if (!world.getName().startsWith(obWorldPrefix)) return;
            if (!world.getPlayers().isEmpty()) return;
            Bukkit.unloadWorld(world, true);
        } catch (Exception e) {
            ErrorLogger.log("Fehler beim Entladen einer leeren Welt (unloadIfEmpty) im WorldManager.java: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<Void> deleteWorld(UUID uuid) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String name = worldName(uuid);
        Player player = Bukkit.getPlayer(uuid);
        debugMsg(player, "Starte Löschung der Welt: " + name);

        Bukkit.getScheduler().runTask(plugin, () -> {
            World world = Bukkit.getWorld(name);
            if (world == null) {
                debugMsg(player, "Welt war nicht geladen.");
                finishDeleteWorld(name, future, player);
                return;
            }

            Location evacLoc = Bukkit.getWorld(Objects.requireNonNull(OneBlock.getInstance().getConfig().getString("OBDelete.TPWorld"))).getSpawnLocation();
            if (evacLoc == null) {
                evacLoc = new Location(Bukkit.getWorld("world"), 0, 100, 0);
                ErrorLogger.log("Teleport Location (world name) für die Evakuierung konnte nicht gefunden werden. Verwende Standard-Welt-Spawn-Position.");
            }
            List<CompletableFuture<Boolean>> teleportFutures = new ArrayList<>();
            for (Player p : world.getPlayers()) {
                teleportFutures.add(p.teleportAsync(evacLoc));
            }

            CompletableFuture.allOf(teleportFutures.toArray(new CompletableFuture[0])).whenComplete((v, tpThrowable) -> {
                if (tpThrowable != null) {
                    ErrorLogger.log("Fehler beim Evakuieren der Spieler in deleteWorld() im WorldManager.java für " + name + ": " + tpThrowable.getMessage(), tpThrowable);
                    future.completeExceptionally(tpThrowable);
                    return;
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        if (!Bukkit.unloadWorld(world, false)) {
                            IllegalStateException ex = new IllegalStateException("Welt konnte nicht entladen werden: " + name);
                            ErrorLogger.log("Welt konnte nicht entladen werden in deleteWorld() im WorldManager.java für " + name, ex);
                            future.completeExceptionally(ex);
                            return;
                        }
                        debugMsg(player, "Welt entladen.");
                        finishDeleteWorld(name, future, player);
                    } catch (Exception e) {
                        ErrorLogger.log("Fehler beim Entladen der Welt in deleteWorld() im WorldManager.java für " + name + ": " + e.getMessage(), e);
                        future.completeExceptionally(e);
                    }
                });
            });
        });

        return future;
    }

    private void finishDeleteWorld(String name, CompletableFuture<Void> future, Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                File worldFolder = new File(Bukkit.getWorldContainer(), name);
                if (worldFolder.exists()) {
                    debugMsg(player, "Lösche Ordner: " + worldFolder.getAbsolutePath());
                    deleteFolder(worldFolder.toPath());
                    debugMsg(player, "Ordner gelöscht.");
                } else {
                    debugMsg(player, "Ordner existiert nicht.");
                }
                future.complete(null);
            } catch (IOException e) {
                debugMsg(player, "Fehler beim Löschen: " + e.getMessage());
                ErrorLogger.log("Fehler beim Löschen einer Welt im WorldManager.java " + e.getMessage(), e);
                future.completeExceptionally(e);
            } catch (Exception e) {
                debugMsg(player, "Unerwarteter Fehler beim Löschen: " + e.getMessage());
                ErrorLogger.log("Unerwarteter Fehler beim Löschen einer Welt im WorldManager.java " + e.getMessage(), e);
                future.completeExceptionally(e);
            }
        });
    }

    private void deleteFolder(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (var walk = Files.walk(path)) {
            walk.sorted((a, b) -> b.compareTo(a)).forEach(file -> {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    ErrorLogger.log("Fehler beim Löschen einer Datei/Ordner (deleteFolder) im WorldManager.java: " + file + " - " + e.getMessage(), e);
                }
            });
        }
    }
}