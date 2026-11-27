package org.GhostLibs.oneBlock.listener;

import org.GhostLibs.oneBlock.events.OBBreakOneblock;
import org.GhostLibs.oneBlock.manager.IslandCache;
import org.GhostLibs.oneBlock.manager.OBManager;
import org.GhostLibs.oneBlock.manager.PhaseManager;
import org.GhostLibs.oneBlock.manager.WorldManager;
import org.GhostLibs.oneBlock.model.IslandPhase;
import org.GhostLibs.oneBlock.model.OBPermission;
import org.GhostLibs.oneBlock.model.OBPhase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BlockListener implements Listener {

    private static final int ONEBLOCK_X = 0;
    private static final int ONEBLOCK_Y = 100;
    private static final int ONEBLOCK_Z = 0;
    private static final int FALLBACK_BLOCK_AMOUNT = 1000;

    private final JavaPlugin plugin;
    private final WorldManager worldManager;
    private final IslandCache cache;
    private OBManager obManager;

    public BlockListener(JavaPlugin plugin, WorldManager worldManager, IslandCache cache, OBManager obManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.cache = cache;
        this.obManager = obManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!worldManager.isOneBlockWorld(block.getWorld())) return;

        Player player = event.getPlayer();

        if (!isOneBlock(block)) return;

        UUID owner = worldManager.getOwnerByWorld(event.getBlock().getWorld());
        IslandCache.Entry entry = cache.get(owner);
        if (entry == null) {
            event.setCancelled(true);
            cache.load(owner);
            return;
        }
        if (!obManager.hasPermission(player.getUniqueId(), entry.island(), OBPermission.BREAK_ONEBLOCK)) {
            event.setCancelled(true);
            return;
        }

        IslandPhase phase = entry.phase();
        Material next = pickMaterial(phase.getSelectedPhaseId());

        OBBreakOneblock obEvent = new OBBreakOneblock(player, entry.island(), phase, block, next);
        Bukkit.getPluginManager().callEvent(obEvent);

        if (obEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        advance(phase);
        entry.markDirty();

        Material result = obEvent.getNextMaterial();
        Location location = block.getLocation();
        Bukkit.getScheduler().runTaskLater(plugin, () -> location.getBlock().setType(result, false), 1L);

        event.setDropItems(false);
        for(ItemStack drop : event.getBlock().getDrops()){
            Bukkit.getScheduler().runTask(plugin, () -> {
                Location spawnLoc = location.clone().add(0.5, 1, 0.5);
                Item item = spawnLoc.getWorld().dropItem(spawnLoc, drop);

                item.setGravity(false);
                item.setVelocity(new Vector(0, 0, 0));
            });
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (!worldManager.isOneBlockWorld(event.getBlock().getWorld())) return;
        event.setCancelled(true);
    }


    private boolean isOneBlock(Block block) {
        return block.getX() == ONEBLOCK_X
                && block.getY() == ONEBLOCK_Y
                && block.getZ() == ONEBLOCK_Z;
    }

    private Material pickMaterial(int phaseId) {
        List<Material> materials = PhaseManager.getPhaseMaterials(phaseId);
        if (materials == null || materials.isEmpty() || materials.contains(Material.AIR)) {
            return Material.DIRT;
        }
        return materials.get(ThreadLocalRandom.current().nextInt(materials.size()));
    }

    private void advance(IslandPhase phase) {
        phase.setMinedBlocks(phase.getMinedBlocks() + 1);

        int left = phase.getBlocksLeft() - 1;
        if (left > 0) {
            phase.setBlocksLeft(left);
            return;
        }

        int old = phase.getCurrentPhaseId();
        int next = Math.min(old + 1, PhaseManager.getHighestPhaseId());
        OBPhase nextPhase = PhaseManager.getPhase(next);

        phase.setCurrentPhaseId(next);
        if (phase.getSelectedPhaseId() == old) {
            phase.setSelectedPhaseId(next);
        }
        phase.setBlocksLeft(nextPhase != null ? nextPhase.getBlockAmount() : FALLBACK_BLOCK_AMOUNT);
    }
}
