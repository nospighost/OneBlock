package de.Main.OneBlock.WorldManager;

import de.Main.OneBlock.Main;
import de.Main.OneBlock.database.DBM;
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
import java.sql.SQLException;
import java.util.*;

public class WorldBorderManager implements Listener {
    private static final Map<UUID, IslandBorderParticles> runningTasks = new HashMap<>();
    private static final Particle defaultParticle = Particle.COMPOSTER;
    private static long lastParticleCheck = 0;
    private static final long particleCheckInterval = 20 * 10; // 30 Sekunden in Ticks
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null) return;

        boolean insideAnyIsland = false;

        List<Island> islands = null;
        try {
            islands = getAllIslands();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        for (Island island : islands) {
            if (island.contains(to.getBlockX(), to.getBlockZ())) {
                insideAnyIsland = true;

                IslandBorderParticles currentTask = runningTasks.get(player.getUniqueId());
                if (currentTask == null || !currentTask.matches(island.centerX, island.centerZ, island.halfSize)) {
                    if (currentTask != null) currentTask.cancel();

                    IslandBorderParticles newTask = new IslandBorderParticles(player, island.centerX, island.centerZ, island.halfSize);
                    newTask.start();
                    runningTasks.put(player.getUniqueId(), newTask);
                }
                break;
            }
        }

        if (!insideAnyIsland) {
            event.setCancelled(true);
            Location from = event.getFrom();
            if (from != null) {
                player.teleport(from);
            }

            IslandBorderParticles task = runningTasks.remove(player.getUniqueId());
            if (task != null) {
                task.cancel();
            }
        }

    }




    private List<Island> getAllIslands() throws SQLException {
        List<Island> islands = new ArrayList<>();
        String query = "SELECT OneBlock_x, OneBlock_z, WorldBorderSize FROM userdata";

        Connection conn = Main.getInstance().getConnection().getConnection(); // Connection offen lassen

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int centerX = rs.getInt("OneBlock_x");
                int centerZ = rs.getInt("OneBlock_z");
                int size = rs.getInt("WorldBorderSize");
                islands.add(new Island(centerX, centerZ, size));
            }

        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Fehler bei getAllIslands: " + e.getMessage());
        }
        // Connection bleibt offen und wird nicht geschlossen
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

        Island island = null;
        try {
            island = getIslandAtLocation(to);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (island == null) {
            event.setCancelled(true);
        }
    }

    private static class IslandBorderParticles extends BukkitRunnable {
        private final Player player;
        private final World world;
        private final int centerX, centerZ, halfSize;

        private Particle particle;

        public IslandBorderParticles(Player player, int centerX, int centerZ, int halfSize) {
            this.player = player;
            this.world = player.getWorld();
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.halfSize = halfSize;
            this.particle = defaultParticle;

            loadParticleFromCurrentIsland();
        }

        private void loadParticleFromCurrentIsland() {
            try {
                Island island = getIslandAtLocation(player.getLocation());
                if (island != null) {
                    String query = "SELECT BorderParticle FROM userdata WHERE OneBlock_x = ? AND OneBlock_z = ? LIMIT 1";
                    Connection conn = Main.getInstance().getConnection().getConnection();
                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setInt(1, island.centerX);
                        ps.setInt(2, island.centerZ);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                String particleName = rs.getString("BorderParticle");
                                if (particleName != null && !particleName.isEmpty()) {
                                    try {
                                        this.particle = Particle.valueOf(particleName.toUpperCase(Locale.ROOT));
                                    } catch (IllegalArgumentException e) {
                                        this.particle = defaultParticle;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    this.particle = defaultParticle;
                }
            } catch (SQLException e) {
                Main.getInstance().getLogger().severe("Fehler beim Laden des Partikels: " + e.getMessage());
                this.particle = defaultParticle;
            }
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

            long now = System.currentTimeMillis();
            if (now - lastParticleCheck > particleCheckInterval * 50) {
                loadParticleFromCurrentIsland();
                lastParticleCheck = now;
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
                    world.spawnParticle(particle, new Location(world, x, y, centerZ - halfSize), 1);
                }
                for (double z = centerZ - halfSize; z <= centerZ + halfSize; z += step) {
                    world.spawnParticle(particle, new Location(world, centerX + halfSize, y, z), 1);
                }
                for (double x = centerX + halfSize; x >= centerX - halfSize; x -= step) {
                    world.spawnParticle(particle, new Location(world, x, y, centerZ + halfSize), 1);
                }
                for (double z = centerZ + halfSize; z >= centerZ - halfSize; z -= step) {
                    world.spawnParticle(particle, new Location(world, centerX - halfSize, y, z), 1);
                }
            }
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
            int maxX = centerX + halfSize;
            int maxZ = centerZ + halfSize;
            return x >= centerX - halfSize && x <= maxX && z >= centerZ - halfSize && z <= maxZ;
        }
    }

    private static Island getIslandAtLocation(Location location) throws SQLException {
        int x = location.getBlockX();
        int z = location.getBlockZ();

        String query = "SELECT OneBlock_x, OneBlock_z, WorldBorderSize FROM userdata";

        Connection conn = Main.getInstance().getConnection().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int centerX = rs.getInt("OneBlock_x");
                int centerZ = rs.getInt("OneBlock_z");
                int size = rs.getInt("WorldBorderSize");

                Island island = new Island(centerX, centerZ, size);
                if (island.contains(x, z)) {
                    return island;
                }
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Fehler bei getIslandAtLocation: " + e.getMessage());
        }
        return null;
    }
}
