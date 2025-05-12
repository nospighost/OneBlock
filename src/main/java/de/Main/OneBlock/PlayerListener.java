package de.Main.OneBlock;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Piston;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static de.Main.OneBlock.Main.oneBlockWorld;

public class PlayerListener implements Listener {

    private static final String WORLD_NAME = "OneBlock";
    private static final Location ONEBLOCK_LOCATION = new Location(Bukkit.getWorld(WORLD_NAME), 0, 100, 0);

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

        if (!config.contains("created") || !config.contains("WorldBorderSize") || !config.contains("owner") || !config.contains("owner-uuid") || !config.contains("location") || !config.contains("EigeneInsel") || !config.contains("z-position") || !config.contains("x-position") || !config.contains("IslandSpawn-x") || !config.contains("IslandSpawn-z")) {
            config.set("created", System.nanoTime());
            config.set("owner", player.getName());
            config.set("owner-uuid", player.getUniqueId().toString());
            config.set("location", "0,100,0");
            config.set("EigeneInsel", false);
            config.set("z-position", 0);
            config.set("x-position", 0);
            config.set("WorldBorderSize", 50);
            config.set("MissingBlocksToLevelUp", 100);
            config.set("IslandLevel", 1);
            Manager.saveIslandConfig(player, config);
        }

        World world = Bukkit.getWorld(WORLD_NAME);
        if (world != null && player.getWorld().getName().equals(WORLD_NAME)) { // Überprüfung, ob der Spieler in der richtigen Welt ist
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
        YamlConfiguration config = Manager.getIslandConfig(player);


        int blockstolevelup = config.getInt("MissingBlocksToLevelUp");
        int IslandLevel = config.getInt("IslandLevel");

        blockstolevelup -= 1;
        config.set("MissingBlocksToLevelUp", blockstolevelup);

        if (blockstolevelup < 1) {
            IslandLevel += 1;
            config.set("IslandLevel", IslandLevel);
            config.set("MissingBlocksToLevelUp", 100);
        }

        Manager.saveIslandConfig(player, config);



        List<String> nextBlocks = Main.config.getStringList("oneblockblocks." + IslandLevel);
        if (nextBlocks.isEmpty()) {
            return;
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(nextBlocks.size());
        String nextBlock = nextBlocks.get(randomIndex);
        Material blockMaterial;
        try {
            blockMaterial = Material.valueOf(nextBlock);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Ungültiger Blockname in der Konfiguration: " + nextBlock);
            blockMaterial = Material.STONE;
        }

        Block block = event.getBlock();
        Location blockLocation = block.getLocation();
        World world = Bukkit.getWorld("OneBlock");
        if (world != null &&
                blockLocation.getWorld().equals(world) &&
                blockLocation.getBlockX() == config.getInt("OneBlock-x") &&
                blockLocation.getBlockY() == 100 &&
                blockLocation.getBlockZ() == config.getInt("OneBlock-z")) {

//Block Drop
            Material originalType = block.getType();
            event.setDropItems(false);
            ItemStack droppedItem = new ItemStack(originalType);
            Item item = block.getWorld().dropItem(blockLocation.clone().add(0.5, 1.0, 0.5), droppedItem);
            item.setVelocity(new Vector(0, 0, 0));
            item.setPickupDelay(10);

            block.setType(Material.AIR);
            regenerateOneBlock(blockLocation, blockMaterial);
            Manager.saveIslandConfig(player, config);
        }
    }

    @EventHandler
    public void onBlockPistonMove(BlockPistonExtendEvent event) {
        event.getBlocks().removeIf(this::isOneBlock);
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
        Material finalBlockMaterial = blockMaterial;
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), () -> {
            Block newBlock = Bukkit.getWorld(WORLD_NAME).getBlockAt(blockLocation);
            newBlock.setType(finalBlockMaterial);
            BlockData blockData = newBlock.getBlockData();
            if (blockData instanceof Piston) {
                newBlock.setBlockData(blockData);
            }
        }, 1L);
    }


}
