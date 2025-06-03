package de.Main.OneBlock.OneBlock.Manager;

import de.Main.OneBlock.Main;

import de.Main.OneBlock.OneBlock.Player.ActionBar;
import de.Main.OneBlock.database.DBM;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import static de.Main.OneBlock.database.DBM.getInt;


public class OneBlockManager implements Listener {
    String prefix = Main.config.getString("Server");
    public static Map<UUID, Boolean> mobSpawning = new HashMap<>();
    public static Map<UUID, Integer> MissingBlocks = new HashMap<>();
    public static Map<UUID, Integer> TotalBlocks = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Player player = event.getPlayer();
        mobSpawning.put(uuid, DBM.getBoolean("userdata", uuid, "mobSpawning", true));
        MissingBlocks.put(uuid, DBM.getInt("userdata", uuid, "MissingBlocksToLevelUp", 100));
        TotalBlocks.put(uuid, DBM.getInt("userdata", uuid, "TotalBlocks", 100));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        savePlayerData(uuid);
        mobSpawning.remove(uuid);
        MissingBlocks.remove(uuid);
        TotalBlocks.remove(uuid);
    }

    public static void startAutoSaveTask() {
        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            for (UUID uuid : MissingBlocks.keySet()) {
                savePlayerData(uuid);
            }
        }, 20L * 30, 20L * 30);
    }

    public static void savePlayerData(UUID uuid) {
        if (mobSpawning.containsKey(uuid))
            mobSpawning.put(uuid, DBM.getBoolean("userdata", uuid, "MobSpawning", true));
        DBM.setBoolean("userdata", uuid, "mobSpawning", mobSpawning.get(uuid));
        if (MissingBlocks.containsKey(uuid))
            DBM.setInt("userdata", uuid, "MissingBlocksToLevelUp", MissingBlocks.get(uuid));
        if (TotalBlocks.containsKey(uuid))
            DBM.setInt("userdata", uuid, "TotalBlocks", TotalBlocks.get(uuid));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();

        UUID ownerUUID = getUUIDFromLocation(blockLocation);

        if (ownerUUID == null) {
            player.sendMessage(prefix + "§cDu darfst hier nichts abbauen! UUID NULL");
            event.setCancelled(true);
            return;
        }

        List<String> trusted = DBM.getList(ownerUUID, "trusted", new ArrayList<>());

        if (!ownerUUID.equals(playerUUID) && !trusted.contains(playerUUID.toString())) {
            player.sendMessage(prefix + "§cDu darfst hier nichts abbauen!");
            event.setCancelled(true);
            return;
        }

        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            String chestName = chest.getCustomName();

            if (chestName != null) {
                int islandLevel = getInt("userdata", ownerUUID, "IslandLevel", 1);
                YamlConfiguration config = (YamlConfiguration) Main.config;
                ConfigurationSection chestsSection = config.getConfigurationSection("oneblockblocks." + islandLevel + ".chests");

                if (chestsSection != null) {
                    List<String> chestKeys = new ArrayList<>(chestsSection.getKeys(false));
                    for (String key : chestKeys) {
                        if (chestName.equalsIgnoreCase(config.getString("oneblockblocks." + islandLevel + ".chests." + key + ".name"))) {
                            Inventory inv = chest.getBlockInventory();
                            for (ItemStack item : inv.getContents()) {
                                if (item != null && item.getType() != Material.AIR) {
                                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                                }
                            }
                            inv.clear();

                            spawnChestWithConfig(blockLocation, key, islandLevel);
                            break;
                        }
                    }
                }
            }
        }


        int blocksToLevelUp = MissingBlocks.getOrDefault(ownerUUID, getInt("userdata", ownerUUID, "MissingBlocksToLevelUp", 100));
        int islandLevel = getInt("userdata", ownerUUID, "IslandLevel", 1);
        boolean durchgespielt = DBM.getBoolean("userdata", ownerUUID, "Durchgespielt", false);

        World oneBlockWorld = Bukkit.getWorld("OneBlock");
        if (oneBlockWorld != null &&
                blockLocation.getWorld().equals(oneBlockWorld) &&
                blockLocation.getBlockX() == getInt("userdata", ownerUUID, "OneBlock_x", 0) &&
                blockLocation.getBlockY() == 100 &&
                blockLocation.getBlockZ() == getInt("userdata", ownerUUID, "OneBlock_z", 0)) {

            int maxLevel = Main.config.getInt("maxlevel");
            int totalBlocks = TotalBlocks.getOrDefault(ownerUUID, getInt("userdata", ownerUUID, "TotalBlocks", 100));

            if (islandLevel != maxLevel) {
                blocksToLevelUp--;
                ActionBar.sendActionbarProgress(player, islandLevel, blocksToLevelUp, totalBlocks, durchgespielt);
            } else {
                ActionBar.sendActionbarProgress(player, islandLevel, blocksToLevelUp, totalBlocks, durchgespielt);
            }

            MissingBlocks.put(ownerUUID, blocksToLevelUp);


            if (blocksToLevelUp <= 0 && islandLevel != maxLevel && DBM.getBoolean("userdata", ownerUUID, "Durchgespielt", false) != true) {
                islandLevel++;
                DBM.setInt("userdata", ownerUUID, "IslandLevel", islandLevel);

                int newTotalBlocks = Main.config.getInt("oneblockblocks." + islandLevel + ".blockcount");

                TotalBlocks.put(ownerUUID, newTotalBlocks);
                MissingBlocks.put(ownerUUID, newTotalBlocks);

                DBM.setInt("userdata", ownerUUID, "TotalBlocks", newTotalBlocks);
                DBM.setInt("userdata", ownerUUID, "MissingBlocksToLevelUp", newTotalBlocks);
            }

            if (islandLevel == 10 && !durchgespielt) {
                DBM.setBoolean("userdata", ownerUUID, "durchgespielt", true);
            }

            List<String> nextBlocks = Main.config.getStringList("oneblockblocks." + islandLevel + ".blocks");
            if (!nextBlocks.isEmpty()) {
                String nextBlock = nextBlocks.get(ThreadLocalRandom.current().nextInt(nextBlocks.size()));
                Material blockMaterial = Material.getMaterial(nextBlock);

                if (blockMaterial == null) {
                    Bukkit.getLogger().warning("Ungültiger Blockname: " + nextBlock);
                    blockMaterial = Material.STONE;
                }

                event.setDropItems(false);
                block.getWorld().dropItemNaturally(blockLocation.clone().add(0.5, 1.0, 0.5), new ItemStack(block.getType()));
                block.setType(Material.AIR);
                Location regenLocation = blockLocation.clone();
                regenLocation.setY(100);
                regenerateOneBlock(regenLocation, blockMaterial, islandLevel);
                if (mobSpawning.get(ownerUUID) == true) {
                    monster(ownerUUID, blockLocation.add(0.5, 1.0, 0.5), islandLevel);
                }


            }
        }
    }


    private void regenerateOneBlock(Location blockLocation, Material blockMaterial, Integer IslandLevel) {
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), () -> {
            Block newBlock = Bukkit.getWorld(de.Main.OneBlock.OneBlock.Player.PlayerListener.WORLD_NAME).getBlockAt(blockLocation);
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


    public void spawnChestWithConfig(Location location, String chestKey, int islandLevel) {
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

    public void monster(UUID ownerUUID, Location spawnLocation, int islandLevel) {
        if (islandLevel == -1) {
            Bukkit.getLogger().warning("Kein IslandLevel für Spieler " + ownerUUID + " in DB gefunden.");
            return;
        }

        YamlConfiguration config = (YamlConfiguration) Main.config; // deine Config-Referenz

        List<Map<?, ?>> monstersList = (List<Map<?, ?>>) config.getList("oneblockblocks." + islandLevel + ".monsters");
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

    public static boolean isLocationOnIsland(UUID ownerUUID, Location location) {
        int centerX = getInt("userdata", ownerUUID, "OneBlock_x", 0);
        int centerZ = getInt("userdata", ownerUUID, "OneBlock_z", 0);
        int diameter = getInt("userdata", ownerUUID, "WorldBorderSize", 50);
        int radius = diameter / 2;

        // Check, ob Location in der Welt OneBlock ist
        if (!location.getWorld().getName().equals("OneBlock")) return false;

        int x = location.getBlockX();
        int z = location.getBlockZ();

        // Prüfen, ob x,z im Bereich ist
        return x >= (centerX - radius) && x < (centerX + radius) &&
                z >= (centerZ - radius) && z < (centerZ + radius);

    }

    public static UUID getUUIDFromLocation(Location location) {
        UUID ownerUUID = null;
        Connection connection = null;
        try {
            connection = Main.getInstance().getConnection().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        int x = location.getBlockX();
        int z = location.getBlockZ();

        String query = "SELECT owner_uuid, OneBlock_x, OneBlock_z, WorldBorderSize FROM userdata";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String uuidString = rs.getString("owner_uuid");
                    int centerX = rs.getInt("OneBlock_x");
                    int centerZ = rs.getInt("OneBlock_z");
                    int radius = rs.getInt("WorldBorderSize");

                    // Prüfe, ob der Block innerhalb des Insel Berreiches s liegt
                    if (x >= centerX - radius && x <= centerX + radius &&
                            z >= centerZ - radius && z <= centerZ + radius) {
                        return UUID.fromString(uuidString);
                    }
                }
            }
        } catch (Exception e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen der UUID von der Location", e);
        }
        return ownerUUID;
    }


}
