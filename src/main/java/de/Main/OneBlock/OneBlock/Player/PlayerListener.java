package de.Main.OneBlock.OneBlock.Player;

import de.Main.OneBlock.Main;
import de.Main.OneBlock.OneBlock.Manager.OneBlockManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class PlayerListener implements Listener {

    private final JavaPlugin plugin;
    public static final String WORLD_NAME = "OneBlock";

    public PlayerListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChestBreak(BlockBreakEvent event) {
        Block block = event.getBlock();


        if (block.getType() != Material.CHEST) return;


        Chest chest = (Chest) block.getState();
        String chestName = chest.getCustomName();


        if (chestName == null) return;

        int islandLevel = getIslandLevelForLocation(block.getLocation());
        if (islandLevel == -1) return;


        FileConfiguration config = Main.getInstance().getConfig();
        ConfigurationSection chestsSection = config.getConfigurationSection("oneblockblocks." + islandLevel + ".chests");

        if (chestsSection == null) return;


        boolean isConfiguredChest = false;

        for (String chestKey : chestsSection.getKeys(false)) {
            String configuredName = config.getString("oneblockblocks." + islandLevel + ".chests." + chestKey + ".name", "§7Kiste");
            if (configuredName.equals(chestName)) {
                isConfiguredChest = true;
                break;
            }
        }

        if (!isConfiguredChest) return;


        Inventory inv = chest.getBlockInventory();
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                block.getWorld().dropItemNaturally(block.getLocation(), item);
            }
        }

        inv.clear();
    }
    private int getIslandLevelForLocation(Location location) {
        int islandLevel = -1;
        Connection connection;

        // Sicherstellen, dass die Location zur Welt "OneBlock" gehört
        if (location.getWorld() == null || !location.getWorld().getName().equals("OneBlock")) {
            return islandLevel;
        }

        try {
            connection = Main.getInstance().getConnection().getConnection(); // Verbindung öffnen
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Aufbau der Datenbankverbindung", e);
        }

        int x = location.getBlockX();
        int z = location.getBlockZ();

        String query = "SELECT IslandLevel, OneBlock_x, OneBlock_z, WorldBorderSize FROM userdata";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int centerX = rs.getInt("OneBlock_x");
                    int centerZ = rs.getInt("OneBlock_z");
                    int radius = rs.getInt("WorldBorderSize") / 2;

                    // Prüfen, ob die Location innerhalb des Inselbereichs liegt
                    if (x >= centerX - radius && x <= centerX + radius &&
                            z >= centerZ - radius && z <= centerZ + radius) {
                        islandLevel = rs.getInt("IslandLevel");
                        break; // Insel gefunden, Schleife beenden
                    }
                }
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen des Insel-Levels von der Location", e);
        }

        return islandLevel;
    }



    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        handlePistonMovement(event.getBlocks(), event);
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        handlePistonMovement(event.getBlocks(), event);
    }

    private void handlePistonMovement(List<Block> blocks, Cancellable event) {
        Connection conn = null; // Verbindung holen
        try {
            conn = Main.getInstance().getConnection().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String query = "SELECT OneBlock_x, OneBlock_z FROM userdata";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                int oneBlockX = rs.getInt("OneBlock_x");
                int oneBlockZ = rs.getInt("OneBlock_z");

                for (Block block : blocks) {
                    if (block.getY() == 100 &&
                            block.getX() == oneBlockX &&
                            block.getZ() == oneBlockZ) {

                        event.setCancelled(true);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Fehler beim Überprüfen der Piston-Bewegung: " + e.getMessage());
        } finally {
            // PreparedStatement und ResultSet schließen
            try {
                if (rs != null) rs.close();
            } catch (Exception ignored) {
            }
            try {
                if (ps != null) ps.close();
            } catch (Exception ignored) {
            }
            // Connection bleibt offen!
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = Main.getInstance().getConnection().getConnection();
            String query = "SELECT OneBlock_x, OneBlock_z, WorldBorderSize FROM userdata";
            ps = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = ps.executeQuery();

            List<Block> blocksToRemove = new ArrayList<>();

            for (Block block : event.blockList()) {
                Location loc = block.getLocation();
                boolean insideIsland = false;

                while (rs.next()) {
                    int centerX = rs.getInt("OneBlock_x");
                    int centerZ = rs.getInt("OneBlock_z");
                    int diameter = rs.getInt("WorldBorderSize");
                    int radius = diameter / 2;

                    int bx = loc.getBlockX();
                    int bz = loc.getBlockZ();

                    if (bx > centerX - radius && bx < centerX + radius &&
                            bz > centerZ - radius && bz < centerZ + radius) {
                        insideIsland = true;
                        break;
                    }

                }
                rs.beforeFirst();

                if (!insideIsland) {
                    blocksToRemove.add(block);
                }
            }

            // Entferne alle Blöcke die außerhalb der Inseln sind
            event.blockList().removeAll(blocksToRemove);

        } catch (Exception e) {
            Bukkit.getLogger().severe("Fehler im Explode-Event: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : null;
        if (loc == null) return; // Kein Block angeklickt
        if (!OneBlockManager.isLocationOnIsland(player.getUniqueId(), loc)) {
            player.sendMessage("§cDu darfst hier nicht interagieren!");
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlockPlaced().getLocation();

        if (!OneBlockManager.isLocationOnIsland(player.getUniqueId(), loc)) {
            player.sendMessage("§cDu darfst hier keine Blöcke platzieren!");
            event.setCancelled(true);
        }
    }

}