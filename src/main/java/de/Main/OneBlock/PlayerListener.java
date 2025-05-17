package de.Main.OneBlock;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Piston;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static de.Main.OneBlock.Manager.getIslandConfig;

public class PlayerListener implements Listener {

    private static final String WORLD_NAME = "OneBlock";
    private static final Location ONEBLOCK_LOCATION = new Location(Bukkit.getWorld(WORLD_NAME), 0, 100, 0);
    private static final String USER_DATA_FOLDER = "plugins/OneBlockPlugin/IslandData";
    String prefix = Main.config.getString("Server");

    private boolean isOneBlock(Block block) {
        World world = block.getWorld();
        return world != null
                && WORLD_NAME.equals(world.getName())
                && block.getLocation().equals(ONEBLOCK_LOCATION);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        YamlConfiguration config = getIslandConfig(player.getUniqueId());

        if (!config.contains("created") || !config.contains("WorldBorderSize") || !config.contains("TotalBlocks") || !config.contains("owner") || !config.contains("owner-uuid") || !config.contains("EigeneInsel") || !config.contains("z-position") || !config.contains("x-position") || !config.contains("IslandSpawn-x") || !config.contains("IslandSpawn-z") || !config.contains("trusted") || !config.contains("added") || !config.contains("invited") || !config.contains("invitedtrust")) {

            config.set("created", System.nanoTime());
            config.set("owner", player.getName());
            config.set("owner-uuid", player.getUniqueId().toString());
            config.set("EigeneInsel", false);
            config.set("z-position", 0);
            config.set("x-position", 0);
            config.set("WorldBorderSize", 50);
            config.set("MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.1.blockcount", 200));
            config.set("TotalBlocks", Main.config.getInt("oneblockblocks.1.blockcount", 200));
            config.set("IslandLevel", 1);
            config.set("OneBlock-x", 0);
            config.set("OneBlock-z", 0);
            config.set("trusted", new ArrayList<String>());
            config.set("invitedtrust", new ArrayList<String>());
            config.set("denied", new ArrayList<String>());

            Manager.saveIslandConfig(player.getUniqueId(), config);
        }

        World world = Bukkit.getWorld(WORLD_NAME);
        if (world != null && player.getWorld().getName().equals(WORLD_NAME)) {
            int x = config.getInt("OneBlock-x", 0);
            int z = config.getInt("OneBlock-z", 0);
            int size = config.getInt("WorldBorderSize", 50);

            WorldBorder border = world.getWorldBorder();
            border.setCenter(x, z);
            border.setSize(size);
            border.setDamageBuffer(0);
            border.setDamageAmount(0.5);
            border.setWarningDistance(5);
            border.setWarningTime(15);

            player.setWorldBorder(border);

            Location spawn = new Location(world, config.getInt("x-position"), 100, config.getInt("z-position"));
            player.teleport(spawn);
        }

        File islandFolder = Main.islandDataFolder;
        if (islandFolder.exists() && islandFolder.isDirectory()) {
            File[] files = islandFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    YamlConfiguration otherConfig = YamlConfiguration.loadConfiguration(file);

                    List<String> addedList = otherConfig.getStringList("added");
                    List<String> trustedList = otherConfig.getStringList("trusted");
                    String ownerName = file.getName().replace(".yml", "");
                }
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
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();

        UUID ownerUUID = getIslandOwnerUUIDByLocation(blockLocation);
        if (ownerUUID == null) {
            player.sendMessage(prefix + "§cDu darfst hier nichts abbauen!");
            player.sendMessage(ownerUUID);
            event.setCancelled(true);
            return;
        }

        YamlConfiguration config = Manager.getIslandConfig(ownerUUID);

        List<String> addedUUIDs = config.getStringList("added");
        List<String> trustedUUIDs = config.getStringList("trusted");

        String playerUUID = player.getUniqueId().toString();

        if (!ownerUUID.equals(player.getUniqueId())
                && !addedUUIDs.contains(playerUUID)
                && !trustedUUIDs.contains(playerUUID)) {

            player.sendMessage(prefix + "§cDu darfst hier nichts abbauen!");
            event.setCancelled(true);
            return;
        }

        int blocksToLevelUp = config.getInt("MissingBlocksToLevelUp");
        int islandLevel = config.getInt("IslandLevel");
        int totalBlocks = config.getInt("TotalBlocks");

        World world = Bukkit.getWorld("OneBlock");

        if (world != null &&
                blockLocation.getWorld().equals(world) &&
                blockLocation.getBlockX() == config.getInt("OneBlock-x") &&
                blockLocation.getBlockY() == 100 &&
                blockLocation.getBlockZ() == config.getInt("OneBlock-z")) {

            blocksToLevelUp -= 1;
            config.set("MissingBlocksToLevelUp", blocksToLevelUp);

            sendActionbarProgress(player, islandLevel, blocksToLevelUp);

            if (blocksToLevelUp <= 0) {
                islandLevel += 1;
                config.set("IslandLevel", islandLevel);
                int newTotal = Main.config.getInt("oneblockblocks." + islandLevel + ".blockcount");
                config.set("TotalBlocks", newTotal);
                config.set("MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks." + islandLevel + ".blockcount"));
            }

            Manager.saveIslandConfig(ownerUUID, config);

            List<String> nextBlocks = Main.config.getStringList("oneblockblocks." + islandLevel + ".blocks");
            if (!nextBlocks.isEmpty()) {
                int randomIndex = ThreadLocalRandom.current().nextInt(nextBlocks.size());
                String nextBlock = nextBlocks.get(randomIndex);

                Material blockMaterial;
                try {
                    blockMaterial = Material.valueOf(nextBlock);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("Ungültiger Blockname in der Konfiguration: " + nextBlock);
                    blockMaterial = Material.STONE;
                }

                event.setDropItems(false);
                ItemStack droppedItem = new ItemStack(block.getType());
                Item item = block.getWorld().dropItem(blockLocation.clone().add(0.5, 1.0, 0.5), droppedItem);
                item.setVelocity(new Vector(0, 0, 0));
                item.setPickupDelay(10);

                block.setType(Material.AIR);
                regenerateOneBlock(blockLocation, blockMaterial);
                monster(ownerUUID, blockLocation.clone().add(0.5, 1.0, 0.5));

            }
        }
    }
    public void monster(UUID ownerUUID, Location spawnLocation) {
        YamlConfiguration config = Manager.getIslandConfig(ownerUUID);
        int islandLevel = config.getInt("IslandLevel");

        List<Map<?, ?>> monstersList = (List<Map<?, ?>>) Main.config.getList("oneblockblocks." + islandLevel + ".monsters");
        if (monstersList == null || monstersList.isEmpty()) return;

        Random random = new Random();

       // hier wird die prozent chance berechntet
        int totalChance = 0;
        for (Map<?, ?> monsterData : monstersList) {
            totalChance += (int) monsterData.get("chance");
        }

        int roll = random.nextInt(100) + 1;  // 1-100

        if (roll > totalChance) {
           //also wenn die % chance nd erreicht ist kein mobser gespawnt
            return;
        }

        // Monster anhand roll auswählen
        int cumulativeChance = 0;
        for (Map<?, ?> monsterData : monstersList) {
            int chance = (int) monsterData.get("chance");
            cumulativeChance += chance;
            if (roll <= cumulativeChance) {
                String monsterName = (String) monsterData.get("monster");
                EntityType entityType;
                try {
                    entityType = EntityType.valueOf(monsterName);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("Ungültiger Monstername in Config: " + monsterName);
                    return;
                }
                spawnLocation.getWorld().spawnEntity(spawnLocation, entityType);
                break; // Nur 1 Monster spawn, danach raus aus der Schleife
            }
        }

    }
    private void sendActionbarProgress(Player player, int currentLevel, int missingBlocks) {
        YamlConfiguration config = getIslandConfig(player.getUniqueId());
        int totalBlocks = config.getInt("TotalBlocks");
        double progress = (double) (totalBlocks - missingBlocks) / totalBlocks;

        int progressLength = (int) (progress * 10);
        StringBuilder bar = new StringBuilder("§7[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < progressLength ? "§a█" : "§7█");
        }
        bar.append("§7]");

        String message = "§bLevel: §e" + currentLevel + " §8| §7" + bar + " §7Noch §c" + missingBlocks + " §7Blöcke bis zum nächsten Level";
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
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
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(this::isOneBlock);
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
            Block oneBlock = Bukkit.getWorld(WORLD_NAME).getBlockAt(ONEBLOCK_LOCATION);
            oneBlock.setType(finalBlockMaterial);
        });
    }

    private void regenerateOneBlock(Location blockLocation, Material blockMaterial) {
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), () -> {
            Block newBlock = Bukkit.getWorld(WORLD_NAME).getBlockAt(blockLocation);
            newBlock.setType(blockMaterial);
            BlockData blockData = newBlock.getBlockData();
            if (blockData instanceof Piston) {
                newBlock.setBlockData(blockData);
            }
        }, 1L);
    }

    private boolean isPlayerAllowed(Location loc, Player player) {
        String islandOwner = getIslandOwnerByLocation(loc);
        if (islandOwner == null) return false;
        return isPlayerAllowedOnIsland(player, UUID.fromString(islandOwner));
    }

    public static boolean isPlayerAllowedOnIsland(Player player, UUID islandOwnerUUID) {
        YamlConfiguration config = Manager.getIslandConfig(islandOwnerUUID);
        List<String> added = config.getStringList("added");
        List<String> trusted = config.getStringList("trusted");

        String playerUUID = player.getUniqueId().toString();

        return player.getUniqueId().equals(islandOwnerUUID)
                || added.contains(playerUUID)
                || trusted.contains(playerUUID);
    }

    public static String getIslandOwnerByLocation(Location loc) {
        File folder = Main.islandDataFolder;
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    int centerX = config.getInt("x-position");
                    int centerZ = config.getInt("z-position");
                    int borderSize = config.getInt("WorldBorderSize", 50);

                    int halfSize = borderSize / 2;
                    if (loc.getWorld().getName().equals("OneBlock") &&
                            loc.getX() >= centerX - halfSize && loc.getX() <= centerX + halfSize &&
                            loc.getZ() >= centerZ - halfSize && loc.getZ() <= centerZ + halfSize) {
                        return file.getName().replace(".yml", "");
                    }
                }
            }
        }
        return null;
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

    public static UUID getIslandOwnerUUIDByLocation(Location loc) {
        File folder = Main.islandDataFolder;
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    int centerX = config.getInt("x-position");
                    int centerZ = config.getInt("z-position");
                    int borderSize = config.getInt("WorldBorderSize", 50);

                    int halfSize = borderSize / 2;
                    if (loc.getWorld().getName().equals(WORLD_NAME) &&
                            loc.getX() >= centerX - halfSize && loc.getX() <= centerX + halfSize &&
                            loc.getZ() >= centerZ - halfSize && loc.getZ() <= centerZ + halfSize) {

                        String ownerUUIDStr = config.getString("owner-uuid");
                        if (ownerUUIDStr != null) {
                            try {
                                return UUID.fromString(ownerUUIDStr);
                            } catch (IllegalArgumentException e) {
                                // Ungültige UUID im File, ignorieren
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
