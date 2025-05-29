package de.Main.OneBlock.Oneblock.Player;

import de.Main.OneBlock.Main;
import de.Main.OneBlock.Oneblock.Manager.Manager;
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
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static de.Main.OneBlock.Oneblock.Manager.Manager.getIslandConfig;


public class PlayerListener implements Listener {
    private final JavaPlugin plugin;

    public static final String WORLD_NAME = "OneBlock";


    private static final String USER_DATA_FOLDER = "plugins/OneBlockPlugin/IslandData";
    String prefix = Main.config.getString("Server");


    public PlayerListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        YamlConfiguration config = getIslandConfig(player.getUniqueId());
        UUID uuid = player.getUniqueId();
        File islandFolder = Main.islandDataFolder;
        if (islandFolder.exists() && islandFolder.isDirectory()) {
            File[] files = islandFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    YamlConfiguration otherConfig = YamlConfiguration.loadConfiguration(file);


                }
            }

            if (!config.contains("created") || !config.contains("WorldBorderSize") || !config.contains("TotalBlocks")) {
                if (!config.contains("created")) {
                    config.set("created", System.nanoTime());
                }
                if (!config.contains("WorldBorderSize")) {
                    config.set("WorldBorderSize", 50);
                }
                if (!config.contains("TotalBlocks")) {
                    config.set("TotalBlocks", Main.config.getInt("oneblockblocks.1.blockcount", 200));
                }
            }

            if (!config.contains("owner") || !config.contains("owner-uuid") || !config.contains("EigeneInsel")) {
                if (!config.contains("owner")) {
                    config.set("owner", player.getName());
                }
                if (!config.contains("owner-uuid")) {
                    config.set("owner-uuid", player.getUniqueId().toString());
                }
                if (!config.contains("EigeneInsel")) {
                    config.set("EigeneInsel", false);
                }
            }

            if (!config.contains("z-position") || !config.contains("x-position")) {
                if (!config.contains("z-position")) {
                    config.set("z-position", 0);
                }
                if (!config.contains("x-position")) {
                    config.set("x-position", 0);
                }
            }

            if (!config.contains("IslandSpawn-x") || !config.contains("IslandSpawn-z")) {
                if (!config.contains("IslandSpawn-x")) {
                    config.set("IslandSpawn-x", 0);
                }
                if (!config.contains("IslandSpawn-z")) {
                    config.set("IslandSpawn-z", 0);
                }
            }

            if (!config.contains("trusted") || !config.contains("invited") || !config.contains("invitedtrust") || !config.contains("denied")) {
                if (!config.contains("trusted")) {
                    config.set("trusted", new ArrayList<String>());
                }
                if (!config.contains("invited")) {
                    config.set("invited", new ArrayList<String>());
                }
                if (!config.contains("invitedtrust")) {
                    config.set("invitedtrust", new ArrayList<String>());
                }
                if (!config.contains("denied")) {
                    config.set("denied", new ArrayList<String>());
                }
            }

            if (!config.contains("MissingBlocksToLevelUp")) {
                config.set("MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.1.blockcount", 200));
            }

            if (!config.contains("IslandLevel")) {
                config.set("IslandLevel", 1);
            }
            if (!config.contains("Durchgespielt")) {
                config.set("Durchgespielt", false);
            }

            if (!config.contains("OneBlock-x") || !config.contains("OneBlock-z")) {
                if (!config.contains("OneBlock-x")) {
                    config.set("OneBlock-x", 0);
                }
                if (!config.contains("OneBlock-z")) {
                    config.set("OneBlock-z", 0);
                }
            }

            Manager.saveIslandConfig(player.getUniqueId(), config);

            World world = Bukkit.getWorld(WORLD_NAME);
            if (world != null && player.getWorld().getName().equals(WORLD_NAME)) {
                Location spawn = new Location(world, config.getInt("x-position"), 100, config.getInt("z-position"));
                player.teleport(spawn);

            }



        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        World world = Bukkit.getWorld(WORLD_NAME);
        YamlConfiguration config = getIslandConfig(player.getUniqueId());
        if (world != null) {
            event.setRespawnLocation(new Location(world, config.getInt("IslandSpawn-x"), 101, config.getInt("IslandSpawn-z")));
        }
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
        for (Block block : blocks) {
            if (block.getY() != 100) continue;

            File folder = new File(USER_DATA_FOLDER);
            if (!folder.exists() || !folder.isDirectory()) return;

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
    public void onEntityExplode(EntityExplodeEvent event) {

        event.blockList().removeIf(block -> isAnyUserOneBlock(block));

        List<String> nextBlocks = Main.config.getStringList("oneblockblocks.block");
        if (nextBlocks.isEmpty()) return;

        int randomIndex = ThreadLocalRandom.current().nextInt(nextBlocks.size());
        String nextBlock = nextBlocks.get(randomIndex);

        Material blockMaterial;
        try {
            blockMaterial = Material.valueOf(nextBlock);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Ungültiger Blockname in der Konfiguration: " + nextBlock);
            blockMaterial = Material.STONE;
        }

        Material finalBlockMaterial = blockMaterial;
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(Main.class), () -> {
            Block oneBlock = (Block) Bukkit.getWorld(WORLD_NAME);
            oneBlock.setType(finalBlockMaterial);
        });
    }


    public boolean isAnyUserOneBlock(Block block) {
        World world = block.getWorld();
        if (world == null || !world.getName().equals(WORLD_NAME)) return false;


        for (UUID ownerUUID : Manager.getAllIslandOwners()) {
            YamlConfiguration config = getIslandConfig(ownerUUID);
            if (config == null) continue;

            int x = config.getInt("OneBlock-x", Integer.MIN_VALUE);
            int y = 100;
            int z = config.getInt("OneBlock-z", Integer.MIN_VALUE);

            Location oneBlockLocation = new Location(world, x, y, z);

            if (block.getLocation().equals(oneBlockLocation)) {
                return true;
            }
        }

        return false;
    }


    private boolean isPlayerAllowed(Location loc, Player player) {
        String islandOwner = Manager.getIslandOwnerByLocation(loc);
        if (islandOwner == null) return false;
        return isPlayerAllowedOnIsland(player, UUID.fromString(islandOwner));
    }

    public static boolean isPlayerAllowedOnIsland(Player player, UUID islandOwnerUUID) {
        YamlConfiguration config = getIslandConfig(islandOwnerUUID);
        List<String> added = config.getStringList("added");
        List<String> trusted = config.getStringList("trusted");

        String playerUUID = player.getUniqueId().toString();

        return player.getUniqueId().equals(islandOwnerUUID)
                || added.contains(playerUUID)
                || trusted.contains(playerUUID);
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Material type = event.getClickedBlock().getType();
        if (!(type == Material.CHEST || type == Material.TRAPPED_CHEST || type == Material.HOPPER || type == Material.SHULKER_BOX))
            return;

        Player player = event.getPlayer();
        if (!isPlayerAllowed(event.getClickedBlock().getLocation(), player)) {
            player.sendMessage(prefix + " §cDu darfst hier nichts öffnen!");
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();

        if (!isPlayerAllowed(event.getBlock().getLocation(), player)) {
            player.sendMessage(prefix + " §cDu darfst hier nichts platzieren!");
            event.setCancelled(true);
        }
    }


}