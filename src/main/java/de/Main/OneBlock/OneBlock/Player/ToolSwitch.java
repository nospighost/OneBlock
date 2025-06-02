package de.Main.OneBlock.OneBlock.Player;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ToolSwitch implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        // Finde den passenden Tool-Typ für den Block
        ToolType toolType = getToolTypeForBlock(blockType);
        if (toolType == null) return; // kein spezielles Tool nötig

        // Suche bestes Tool im Inventar
        int bestSlot = findBestToolSlot(player, toolType);

        if (bestSlot != -1 && bestSlot != player.getInventory().getHeldItemSlot()) {
            player.getInventory().setHeldItemSlot(bestSlot);
        }
    }

    // Welche Tools sind gut für welchen Block?
    private ToolType getToolTypeForBlock(Material block) {
        List<Material> pickaxeBlocks = Arrays.asList(
                Material.STONE, Material.COBBLESTONE, Material.IRON_ORE, Material.COAL_ORE,
                Material.GOLD_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE,
                Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE, Material.ANCIENT_DEBRIS
        );
        List<Material> axeBlocks = Arrays.asList(
                Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG,
                Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.STRIPPED_OAK_LOG,
                Material.STRIPPED_SPRUCE_LOG, Material.STRIPPED_BIRCH_LOG, Material.STRIPPED_JUNGLE_LOG,
                Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
                Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS,
                Material.JUNGLE_PLANKS, Material.ACACIA_PLANKS, Material.DARK_OAK_PLANKS
        );
        List<Material> shovelBlocks = Arrays.asList(
                Material.DIRT, Material.GRASS_BLOCK, Material.SAND, Material.GRAVEL, Material.CLAY,
                Material.SOUL_SAND, Material.MYCELIUM
        );

        if (pickaxeBlocks.contains(block)) return ToolType.PICKAXE;
        if (axeBlocks.contains(block)) return ToolType.AXE;
        if (shovelBlocks.contains(block)) return ToolType.SHOVEL;

        return null;
    }

    // Suche bestes Tool des Typs im Inventar
    private int findBestToolSlot(Player player, ToolType toolType) {
        int bestSlot = -1;
        int bestLevel = -1;

        for (int slot = 0; slot < 9; slot++) { // nur Hotbar
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null) continue;

            if (isToolType(item.getType(), toolType)) {
                int level = getToolLevel(item.getType());
                if (level > bestLevel) {
                    bestLevel = level;
                    bestSlot = slot;
                }
            }
        }
        return bestSlot;
    }


    private boolean isToolType(Material mat, ToolType type) {
        switch (type) {
            case PICKAXE:
                return mat == Material.WOODEN_PICKAXE || mat == Material.STONE_PICKAXE || mat == Material.IRON_PICKAXE ||
                        mat == Material.GOLDEN_PICKAXE || mat == Material.DIAMOND_PICKAXE || mat == Material.NETHERITE_PICKAXE;
            case AXE:
                return mat == Material.WOODEN_AXE || mat == Material.STONE_AXE || mat == Material.IRON_AXE ||
                        mat == Material.GOLDEN_AXE || mat == Material.DIAMOND_AXE || mat == Material.NETHERITE_AXE;
            case SHOVEL:
                return mat == Material.WOODEN_SHOVEL || mat == Material.STONE_SHOVEL || mat == Material.IRON_SHOVEL ||
                        mat == Material.GOLDEN_SHOVEL || mat == Material.DIAMOND_SHOVEL || mat == Material.NETHERITE_SHOVEL;
            default:
                return false;
        }
    }

    // Bestimme "Level" des Tools (Holz=1, Stein=2, Eisen=3, Gold=2, Diamant=4, Netherite=5)
    private int getToolLevel(Material mat) {
        switch (mat) {
            case WOODEN_PICKAXE:
            case WOODEN_AXE:
            case WOODEN_SHOVEL:
                return 1;
            case STONE_PICKAXE:
            case STONE_AXE:
            case STONE_SHOVEL:
                return 2;
            case IRON_PICKAXE:
            case IRON_AXE:
            case IRON_SHOVEL:
                return 3;
            case GOLDEN_PICKAXE:
            case GOLDEN_AXE:
            case GOLDEN_SHOVEL:
                return 2; // Gold ist schnell, aber haltbar schlecht
            case DIAMOND_PICKAXE:
            case DIAMOND_AXE:
            case DIAMOND_SHOVEL:
                return 4;
            case NETHERITE_PICKAXE:
            case NETHERITE_AXE:
            case NETHERITE_SHOVEL:
                return 5;
            default:
                return 0;
        }
    }

    enum ToolType {
        PICKAXE,
        AXE,
        SHOVEL
    }
}
