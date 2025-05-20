package de.Main.OneBlock;

import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;


import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WorldBorderManager implements Listener {

    private static final HashMap<UUID, IslandBorderParticles> runningTasks = new HashMap<>();
    private static final Map<UUID, IslandBorderParticles> runningTasks1 = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Location to = event.getTo();
        if (to == null) return;

        File folder = new File("plugins/oneblockplugin/IslandData");
        if (!folder.exists() || !folder.isDirectory()) return;

        boolean foundIsland = false;

        for (File file : folder.listFiles()) {
            if (!file.getName().endsWith(".yml")) continue;

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            int centerX = config.getInt("x-position");
            int centerZ = config.getInt("z-position");
            int size = config.getInt("WorldBorderSize");
            int half = (int)(size / 2.0 - 0.5);

            double minX = centerX - half;
            double maxX = centerX + half;
            double minZ = centerZ - half;
            double maxZ = centerZ + half;

            if (to.getX() >= minX && to.getX() <= maxX && to.getZ() >= minZ && to.getZ() <= maxZ) {


                    foundIsland = true;

                    IslandBorderParticles currentTask = runningTasks.get(playerUUID);
                    if (currentTask == null || !currentTask.matches(centerX, centerZ, half)) {

                        if (currentTask != null) {
                            currentTask.cancel();
                        }
                        // Neuen Task starten
                        IslandBorderParticles newTask = new IslandBorderParticles(player, centerX, centerZ, half);
                        newTask.start();
                        runningTasks.put(playerUUID, newTask);
                    }

                    break;

            }
        }

        if (!foundIsland) {
            event.setCancelled(true);
            Location from = event.getFrom();
            if (from != null) {
                player.teleport(from);
            }
            IslandBorderParticles task = runningTasks.get(playerUUID);
            if (task != null) {
                task.cancel();
                runningTasks.remove(playerUUID);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (runningTasks.containsKey(uuid)) {
            runningTasks.get(uuid).cancel();
            runningTasks.remove(uuid);
        }
    }
    // Innere Klasse für pulsierende Partikel-Border
    private static class IslandBorderParticles extends BukkitRunnable {

        private final Player player;
        private final World world;
        private final int centerX, centerZ, halfSize;
        private int tick = 0;

        public IslandBorderParticles(Player player, int centerX, int centerZ, int halfSize) {
            this.player = player;
            this.world = player.getWorld();
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.halfSize = halfSize;
        }

        public void start() {
            // Task alle 5 Ticks (0,25 Sek)
            this.runTaskTimer(Main.getPlugin(), 0L, 3L);
        }

        @Override
        public void run() {
            if (!player.isOnline()) {
                this.cancel();
                WorldBorderManager.runningTasks.remove(player.getUniqueId()); // Task entfernen wenn abgebrochen
                return;
            }
            tick++;
            spawnBorderParticles();
        }


        private void spawnBorderParticles() {
            double baseY = player.getLocation().getY();  // Spielerhöhe

            double step = 1.0;

            // Höhen: 1 Block runter, dann 5 Blöcke hoch (insgesamt 6 Ebenen)
            int[] heights = {-1, 0, 1, 2, 3, 4 };

            for (int h : heights) {
                double y = Math.floor(baseY) + h;  // Ganze Blöcke, damit es sauber aussieht

                for (double x = centerX - halfSize; x <= centerX + halfSize; x += step) {
                    spawnParticleAt(x, y, centerZ - halfSize);
                }
                for (double z = centerZ - halfSize; z <= centerZ + halfSize; z += step) {
                    spawnParticleAt(centerX + halfSize, y, z);
                }
                for (double x = centerX + halfSize; x >= centerX - halfSize; x -= step) {
                    spawnParticleAt(x, y, centerZ + halfSize);
                }
                for (double z = centerZ + halfSize; z >= centerZ - halfSize; z -= step) {
                    spawnParticleAt(centerX - halfSize, y, z);
                }
            }
        }

        private void spawnParticleAt(double x, double y, double z) {
            Location loc = new Location(world, x, y, z);
            world.spawnParticle(Particle.COMPOSTER, loc, 1);

        }


        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            WorldBorderManager.runningTasks.remove(player.getUniqueId()); // Sicherstellen, dass Task entfernt wird
        }

        public boolean matches(int centerX, int centerZ, int halfSize) {
            return this.centerX == centerX && this.centerZ == centerZ && this.halfSize == halfSize;
        }

    }

}