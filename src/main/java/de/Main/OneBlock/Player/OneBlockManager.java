package de.Main.OneBlock.Player;

import de.Main.OneBlock.Main;
import de.Main.OneBlock.Manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import static de.Main.OneBlock.Manager.Manager.getIslandConfig;

public class OneBlockManager implements Listener {
    String prefix = Main.config.getString("Server");
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();

        UUID ownerUUID = Manager.getIslandOwnerUUIDByLocation(blockLocation);
        if (ownerUUID == null) {
            player.sendMessage(prefix + "§cDu darfst hier nichts abbauen!");
            event.setCancelled(true);
            return;
        }

        YamlConfiguration config = getIslandConfig(ownerUUID);
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


        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            String chestName = chest.getCustomName();

            if (chestName != null) {
                int islandLevel = config.getInt("IslandLevel");

                ConfigurationSection chestsSection = Main.getInstance().getConfig()
                        .getConfigurationSection("oneblockblocks." + islandLevel + ".chests");

                if (chestsSection != null) {
                    for (String chestKey : chestsSection.getKeys(false)) {
                        String configuredName = Main.getInstance().getConfig()
                                .getString("oneblockblocks." + islandLevel + ".chests." + chestKey + ".name", "§7Kiste");

                        if (configuredName.equals(chestName)) {
                            Inventory inv = chest.getBlockInventory();
                            for (ItemStack item : inv.getContents()) {
                                if (item != null && item.getType() != Material.AIR) {
                                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                                }
                            }
                            inv.clear();
                            break;
                        }
                    }
                }
            }
        }


        int blocksToLevelUp = config.getInt("MissingBlocksToLevelUp");
        int islandLevel = config.getInt("IslandLevel");
        boolean durchgespielt = config.getBoolean("Durchgespielt");
        World world = Bukkit.getWorld("OneBlock");



        if (world != null &&
                blockLocation.getWorld().equals(world) &&
                blockLocation.getBlockX() == config.getInt("OneBlock-x") &&
                blockLocation.getBlockY() == 100 &&
                blockLocation.getBlockZ() == config.getInt("OneBlock-z")) {

            int maxlevel = Main.config.getInt("maxlevel");
            if (islandLevel != maxlevel) {
                blocksToLevelUp -= 1;
                ActionBar.sendActionbarProgress(player, islandLevel, blocksToLevelUp);
            } else {
                ActionBar.sendActionbarProgress(player, islandLevel, Integer.MIN_VALUE);
            }

            config.set("MissingBlocksToLevelUp", blocksToLevelUp);

            if (blocksToLevelUp <= 0 && islandLevel != maxlevel) {
                islandLevel += 1;
                config.set("IslandLevel", islandLevel);
                int newTotal = Main.config.getInt("oneblockblocks." + islandLevel + ".blockcount");
                config.set("TotalBlocks", newTotal);
                config.set("MissingBlocksToLevelUp", newTotal);
            }

            if (islandLevel == 10 && !durchgespielt) {
                config.set("Durchgespielt", true);
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
                regenerateOneBlock(blockLocation, blockMaterial, islandLevel);
                monster(ownerUUID, blockLocation.clone().add(0.5, 1.0, 0.5));
            }
        }
    }
    private void regenerateOneBlock(Location blockLocation, Material blockMaterial, Integer IslandLevel) {
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), () -> {
            Block newBlock = Bukkit.getWorld(PlayerListener.WORLD_NAME).getBlockAt(blockLocation);
            newBlock.setType(blockMaterial);

            if (blockMaterial == Material.CHEST) {
                YamlConfiguration config = (YamlConfiguration) Main.config;
                newBlock.setType(Material.CHEST);

                ConfigurationSection chestsSection = config.getConfigurationSection("oneblockblocks." + IslandLevel + ".chests");
                if (chestsSection == null || chestsSection.getKeys(false).isEmpty()) {
                    Bukkit.getLogger().warning("Keine Kisten für Level " + IslandLevel + " gefunden!");
                    return;
                }

                List<String> chestKeys = new ArrayList<>(chestsSection.getKeys(false));
                String randomChestKey = chestKeys.get(new Random().nextInt(chestKeys.size()));


                Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), () -> {
                    spawnChestWithConfig(blockLocation, randomChestKey, IslandLevel);
                }, 1L);
            }
        }, 1L);
    }

    public void spawnChestWithConfig (Location location, String chestKey,int islandLevel) {
        YamlConfiguration config = (YamlConfiguration) Main.config;

        String path = "oneblockblocks." + islandLevel + ".chests." + chestKey;

        if (!config.contains(path)) {
            Bukkit.getLogger().warning("Chest-Key nicht in Config gefunden: " + chestKey + " für Level " + islandLevel);
            return;
        }

        String name = config.getString(path + ".name", "§7Kiste");
        List<String> contents = config.getStringList(path + ".contents");

        Block block = location.getBlock();
        block.setType(Material.CHEST);
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), () -> {
            Block updatedBlock = location.getBlock();
            if (updatedBlock.getType() != Material.CHEST) {
                Bukkit.getLogger().warning("Block ist nach Delay keine Chest: " + updatedBlock.getType());
                return;
            }

            Chest chest = (Chest) updatedBlock.getState();
            chest.setCustomName(name);
            chest.update();

            Inventory inv = chest.getBlockInventory();
            inv.clear();

            for (String itemString : contents) {
                String[] parts = itemString.split(":");
                Material mat = Material.matchMaterial(parts[0].toUpperCase());
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

                if (mat != null) {
                    inv.addItem(new ItemStack(mat, amount));
                }
            }
        }, 1L);

    }

    public void monster (UUID ownerUUID, Location spawnLocation){
        YamlConfiguration config = getIslandConfig(ownerUUID);
        int islandLevel = config.getInt("IslandLevel");

        List<Map<?, ?>> monstersList = (List<Map<?, ?>>) Main.config.getList("oneblockblocks." + islandLevel + ".monsters");
        if (monstersList == null || monstersList.isEmpty()) return;

        Random random = new Random();


        int totalChance = 0;
        for (Map<?, ?> monsterData : monstersList) {
            totalChance += (int) monsterData.get("chance");
        }

        int roll = random.nextInt(100) + 1;

        if (roll > totalChance) {
            return;
        }


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
                break;
            }
        }

    }
}
