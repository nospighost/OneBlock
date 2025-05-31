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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerListener implements Listener {
    private final JavaPlugin plugin;

    public static final String WORLD_NAME = "OneBlock";


    private static final String USER_DATA_FOLDER = "plugins/OneBlockPlugin/IslandData";


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
        File folder = Main.islandDataFolder;
        if (!folder.exists() || !folder.isDirectory()) return -1;

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return -1;

        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            int centerX = config.getInt("x-position");
            int centerZ = config.getInt("z-position");
            int borderSize = config.getInt("WorldBorderSize", 50);
            int halfSize = borderSize / 2;

            if (location.getWorld() != null && location.getWorld().getName().equals("OneBlock")
                    && location.getBlockX() >= centerX - halfSize
                    && location.getBlockX() <= centerX + halfSize
                    && location.getBlockZ() >= centerZ - halfSize
                    && location.getBlockZ() <= centerZ + halfSize) {
                return config.getInt("IslandLevel", 1);
            }
        }

        return -1;
    }


    @EventHandler
    public void onBlockPiston(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.getY() != 100) continue;

            File folder = new File(USER_DATA_FOLDER);
            for (File file : folder.listFiles()) {
                if (!file.getName().endsWith(".yml")) continue;

                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                int x = config.getInt("OneBlock-x");
                int z = config.getInt("OneBlock-z");
                World world = Bukkit.getWorld("OneBlock");

                if (world != null) {
                    Location oneBlockLocation = new Location(world, x, 100, z);
                    if (block.getLocation().equals(oneBlockLocation)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
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
        Connection conn = Main.getInstance().getConnection().getConnection(); // Verbindung holen

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
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(Main.class), () -> {
            Connection conn = Main.getInstance().getConnection().getConnection(); // Verbindung holen
            String query = "SELECT OneBlock_x, OneBlock_z FROM userdata";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = conn.prepareStatement(query);
                rs = ps.executeQuery();

                World world = Bukkit.getWorld("OneBlock");
                if (world == null) return;

                while (rs.next()) {
                    int oneBlockX = rs.getInt("OneBlock_x");
                    int oneBlockZ = rs.getInt("OneBlock_z");
                    Location oneBlockLocation = new Location(world, oneBlockX, 100, oneBlockZ);

                    // Überprüfen, ob die Explosion den OneBlock betrifft
                    for (Block block : event.blockList()) {
                        if (block.getLocation().equals(oneBlockLocation)) {
                            // Explosion abbrechen
                            event.setCancelled(true);
                            block.setType(Material.DIRT);
                            return; // Kein weiterer Check nötig
                        }
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Fehler beim Verarbeiten von EntityExplodeEvent: " + e.getMessage());
            } finally {
                // Ressourcen schließen
                try {
                    if (rs != null) rs.close();
                } catch (Exception ignored) {
                }
                try {
                    if (ps != null) ps.close();
                } catch (Exception ignored) {
                }
            }
        });
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