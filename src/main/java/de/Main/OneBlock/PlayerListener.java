package de.Main.OneBlock;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Piston;
import org.bukkit.configuration.file.YamlConfiguration;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static de.Main.OneBlock.Main.config;
import static de.Main.OneBlock.Main.oneBlockWorld;

public class PlayerListener implements Listener {

    private static final String WORLD_NAME = "OneBlock";
    private static final Location ONEBLOCK_LOCATION = new Location(Bukkit.getWorld(WORLD_NAME), 0, 100, 0);
    private static final String USER_DATA_FOLDER = "plugins/OneBlockPlugin/IslandData";

    private boolean isOneBlock(Block block) {
        World world = block.getWorld();
        return world != null
                && WORLD_NAME.equals(world.getName())
                && block.getLocation().equals(ONEBLOCK_LOCATION);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        YamlConfiguration config = Manager.getIslandConfig(player);


        if (!config.contains("created") || !config.contains("WorldBorderSize") || !config.contains("TotalBlocks") || !config.contains("owner") || !config.contains("owner-uuid") || !config.contains("EigeneInsel") || !config.contains("z-position") || !config.contains("x-position") || !config.contains("IslandSpawn-x") || !config.contains("IslandSpawn-z") || !config.contains("trusted") || !config.contains("added") || !config.contains("invited") || !config.contains("invitedtrust")) {

            config.set("created", System.nanoTime());
            config.set("owner", player.getName());
            config.set("owner-uuid", player.getUniqueId().toString());
            config.set("EigeneInsel", false);
            config.set("z-position", 0);
            config.set("x-position", 0);
            config.set("WorldBorderSize", 50);
            config.set("MissingBlocksToLevelUp", 200);
            config.set("TotalBlocks", 200);
            config.set("IslandLevel", 1);
            config.set("OneBlock-x", 0);
            config.set("OneBlock-z", 0);

            config.set("trusted", new ArrayList<String>());
            config.set("added", new ArrayList<String>());
            config.set("invited", new ArrayList<String>());
            config.set("invitedtrust", new ArrayList<String>());

            Manager.saveIslandConfig(player, config);
        }


        // WorldBorder und Teleport
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

        // *** Hier prüfen, ob der Spieler in trusted/added Listen von anderen Inseln steht ***
        File islandFolder = Main.islandDataFolder;
        if (islandFolder.exists() && islandFolder.isDirectory()) {
            File[] files = islandFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    YamlConfiguration otherConfig = YamlConfiguration.loadConfiguration(file);

                    List<String> addedList = otherConfig.getStringList("added");
                    List<String> trustedList = otherConfig.getStringList("trusted");
                    String ownerName = file.getName().replace(".yml", "");

                    if (addedList.contains(player.getName())) {
                        player.sendMessage("§aDu bist als Mitglied auf der Insel von §e" + ownerName + " §aeingetragen.");
                        // Hier kannst du noch mehr Aktionen machen (Permissions etc.)
                    }

                    if (trustedList.contains(player.getName())) {
                        player.sendMessage("§aDu bist als Vertrauensspieler auf der Insel von §e" + ownerName + " §aeingetragen.");
                        // Hier kannst du noch mehr Aktionen machen (Permissions etc.)
                    }
                }
            }
        }
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        World world = Bukkit.getWorld(WORLD_NAME);
        YamlConfiguration config = Manager.getIslandConfig(player);
        if (world != null) {
            event.setRespawnLocation(new Location(world, config.getInt("IslandSpawn-x"), 101, config.getInt("IslandSpawn-z")));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();

        String ownerName = getIslandOwnerByLocation(blockLocation);
        if (ownerName == null) {
            player.sendMessage("§cDu darfst hier nichts abbauen!");
            event.setCancelled(true);
            return;
        }

        File islandFile = new File(Main.islandDataFolder, ownerName + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(islandFile);

        List<String> added = config.getStringList("added");
        List<String> trusted = config.getStringList("trusted");

        if (!ownerName.equals(player.getName()) &&
                !added.contains(player.getName()) &&
                !trusted.contains(player.getName())) {

            player.sendMessage("§cDu darfst hier nichts abbauen!");
            event.setCancelled(true);
            return;
        }//t

        int blockstolevelup = config.getInt("MissingBlocksToLevelUp");
        int IslandLevel = config.getInt("IslandLevel");
        int totalblocks = config.getInt("TotalBlocks");

        World world = Bukkit.getWorld("OneBlock");

        if (world != null &&
                blockLocation.getWorld().equals(world) &&
                blockLocation.getBlockX() == config.getInt("OneBlock-x") &&
                blockLocation.getBlockY() == 100 &&
                blockLocation.getBlockZ() == config.getInt("OneBlock-z")) {

            blockstolevelup -= 1;
            config.set("MissingBlocksToLevelUp", blockstolevelup);

            sendActionbarProgress(player, IslandLevel, blockstolevelup);

            if (blockstolevelup == 0) {
                IslandLevel += 1;
                config.set("IslandLevel", IslandLevel);
                int v = totalblocks * 2;
                config.set("TotalBlocks", v);
                config.set("MissingBlocksToLevelUp", v);
            }

            Manager.saveIslandConfig(Bukkit.getPlayer(ownerName), config); // speichert für Besitzer

            List<String> nextBlocks = Main.config.getStringList("oneblockblocks." + IslandLevel);
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

            event.setDropItems(false);
            ItemStack droppedItem = new ItemStack(block.getType());
            Item item = block.getWorld().dropItem(blockLocation.clone().add(0.5, 1.0, 0.5), droppedItem);
            item.setVelocity(new Vector(0, 0, 0));
            item.setPickupDelay(10);

            block.setType(Material.AIR);
            regenerateOneBlock(blockLocation, blockMaterial);
            Manager.saveIslandConfig(Bukkit.getPlayer(ownerName), config);
        }
    }


    private void sendActionbarProgress(Player player, int currentLevel, int missingBlocks) {
        YamlConfiguration config = Manager.getIslandConfig(player);
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
        return isPlayerAllowedOnIsland(player, islandOwner);
    }

    public static boolean isPlayerAllowedOnIsland(Player player, String islandOwner) {
        File file = new File(Main.islandDataFolder, islandOwner + ".yml");
        if (!file.exists()) return false;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> added = config.getStringList("added");
        List<String> trusted = config.getStringList("trusted");

        return player.getName().equalsIgnoreCase(islandOwner)
                || added.contains(player.getName())
                || trusted.contains(player.getName());
    }


    public String getIslandOwnerByLocation(Location loc) {
        File folder = new File(USER_DATA_FOLDER);
        if (!folder.exists()) return null;

        for (File file : folder.listFiles()) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            int x = config.getInt("OneBlock-x");
            int z = config.getInt("OneBlock-z");
            int size = config.getInt("WorldBorderSize", 50);
            int half = size / 2;

            if (loc.getWorld().getName().equals(WORLD_NAME) &&
                    loc.getBlockX() >= x - half && loc.getBlockX() <= x + half &&
                    loc.getBlockZ() >= z - half && loc.getBlockZ() <= z + half) {
                return file.getName().replace(".yml", "");
            }
        }
        return null;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Material type = event.getClickedBlock().getType();
        if (!(type == Material.CHEST || type == Material.TRAPPED_CHEST || type == Material.HOPPER || type == Material.SHULKER_BOX)) return;

        Player player = event.getPlayer();
        if (!isPlayerAllowed(event.getClickedBlock().getLocation(), player)) {
            player.sendMessage("§cDu darfst hier nichts öffnen!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerAllowed(event.getBlock().getLocation(), player)) {
            player.sendMessage("§cDu darfst hier nichts platzieren!");
            event.setCancelled(true);
        }
    }
}
