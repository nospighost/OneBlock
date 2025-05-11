package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Piston;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
        World world = Bukkit.getWorld(WORLD_NAME);
        if (world != null) {
            event.getPlayer().teleport(new Location(world, 0, 101, 0));
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        World world = Bukkit.getWorld(WORLD_NAME);
        if (world != null) {
            event.setRespawnLocation(new Location(world, 0, 101, 0));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        String Level = "Oberwelt"; // der Wert kommt später irgendwoher

        List<String> nextBlocks = Main.config.getStringList("oneblockblocks." + Level);

        if (nextBlocks.isEmpty()) {
            return;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(nextBlocks.size());

        String nextBlock = nextBlocks.get(randomIndex);




        Material blockMaterial = null;
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
                blockLocation.getBlockX() == 0 &&
                blockLocation.getBlockY() == 100 &&
                blockLocation.getBlockZ() == 0) {

            block.setType(Material.AIR);

            regenerateOneBlock(blockLocation, blockMaterial);
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
