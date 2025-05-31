package de.Main.OneBlock.WorldManager;

import de.Main.OneBlock.Main;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class WorldBorderManager implements Listener {

    private static final Map<UUID, IslandBorderParticles> runningTasks = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null) return;

        boolean insideAnyIsland = false;

        // Alle Inseln prüfen
        List<Island> islands = getAllIslands();

        for (Island island : islands) {
            if (island.contains(to.getBlockX(), to.getBlockZ())) {
                insideAnyIsland = true;

                // Optional: Spawn Partikel nur bei der aktuellen Insel, wenn du willst

                IslandBorderParticles currentTask = runningTasks.get(player.getUniqueId());

                if (currentTask == null || !currentTask.matches(island.centerX, island.centerZ, island.halfSize)) {
                    if (currentTask != null) currentTask.cancel();

                    IslandBorderParticles newTask = new IslandBorderParticles(player, island.centerX, island.centerZ, island.halfSize);
                    newTask.start();
                    runningTasks.put(player.getUniqueId(), newTask);
                }
                break; // Spieler ist in einer Insel, fertig
            }
        }

        if (!insideAnyIsland) {
            // Spieler ist außerhalb aller Inseln, Bewegung abbrechen
            event.setCancelled(true);
            Location from = event.getFrom();
            if (from != null) {
                player.teleport(from);
            }

            IslandBorderParticles task = runningTasks.get(player.getUniqueId());
            if (task != null) {
                task.cancel();
                runningTasks.remove(player.getUniqueId());
            }
        }
    }

    private List<Island> getAllIslands() {
        List<Island> islands = new ArrayList<>();
        String query = "SELECT OneBlock_x, OneBlock_z, WorldBorderSize FROM userdata";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Main.getInstance().getConnection().getConnection();
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                int centerX = rs.getInt("OneBlock_x");
                int centerZ = rs.getInt("OneBlock_z");
                int size = rs.getInt("WorldBorderSize");

                islands.add(new Island(centerX, centerZ, size));
            }
        } catch (Exception e) {
            Main.getInstance().getLogger().severe("Fehler bei getAllIslands: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception ignored) {}
            try {
                if (ps != null) ps.close();
            } catch (Exception ignored) {}

        }

        return islands;
    }




    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        IslandBorderParticles task = runningTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        if (to == null) return;

        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        Island island = getIslandAtLocation(to);

        if (island == null) {
            event.setCancelled(true);
        }
    }

    private static class IslandBorderParticles extends BukkitRunnable {
        private final Player player;
        private final World world;
        private final int centerX, centerZ, halfSize;

        public IslandBorderParticles(Player player, int centerX, int centerZ, int halfSize) {
            this.player = player;
            this.world = player.getWorld();
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.halfSize = halfSize;
        }

        public void start() {
            this.runTaskTimer(Main.getPlugin(), 0L, 3L);
        }

        @Override
        public void run() {
            if (!player.isOnline()) {
                this.cancel();
                runningTasks.remove(player.getUniqueId());
                return;
            }
            spawnBorderParticles();
        }

        private void spawnBorderParticles() {
            double baseY = player.getLocation().getY();
            double step = 1.0;
            int[] heights = {-1, 0, 1, 2, 3, 4};

            for (int h : heights) {
                double y = Math.floor(baseY) + h;

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
            runningTasks.remove(player.getUniqueId());
        }

        public boolean matches(int centerX, int centerZ, int halfSize) {
            return this.centerX == centerX && this.centerZ == centerZ && this.halfSize == halfSize;
        }
    }

    private static class Island {
        final int centerX, centerZ, size, halfSize;

        public Island(int centerX, int centerZ, int size) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.size = size;
            this.halfSize = size / 2;
        }

        public boolean contains(double x, double z) {
            int maxX = centerX + (size % 2 == 0 ? halfSize - 1 : halfSize);
            int maxZ = centerZ + (size % 2 == 0 ? halfSize - 1 : halfSize);

            return x >= centerX - halfSize && x <= maxX
                    && z >= centerZ - halfSize && z <= maxZ;
        }
    }

    private Island getIslandAtLocation(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();

        String query = "SELECT OneBlock_x, OneBlock_z, WorldBorderSize FROM userdata";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Main.getInstance().getConnection().getConnection();

            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                int centerX = rs.getInt("OneBlock_x");
                int centerZ = rs.getInt("OneBlock_z");
                int size = rs.getInt("WorldBorderSize");

                Island island = new Island(centerX, centerZ, size);

                if (island.contains(x, z)) {
                    return island;
                }
            }
        } catch (Exception e) {
            Main.getInstance().getLogger().severe("Fehler bei DB-Abfrage im WorldBorderManager: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception ignored) {
            }
            try {
                if (ps != null) ps.close();
            } catch (Exception ignored) {
            }
            // Verbindung wird NICHT geschlossen hier
        }
        return null;
    }
}
