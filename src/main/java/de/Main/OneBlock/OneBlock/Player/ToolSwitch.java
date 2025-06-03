package de.Main.OneBlock.OneBlock.Player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;

public class ToolSwitch implements Listener {


    private static final EnumMap<Material, ToolType> TOOL_ASSIGNMENTS = new EnumMap<>(Material.class);

    static {

        for (Material material : Material.values()) {
            if (material.isBlock()) {

                if (material.name().contains("STONE") ||material.name().contains("MYCELIUM") ||material.name().contains("ICE") || material.name().contains("ORE") || material.name().contains("QUARTZ")|| material.name().contains("TERRACOTTA") ||material.name().contains("TUFF") ||
                        material.name().contains("DEBRIS") || material.name().contains("TILE") ||material.name().contains("BASALT") ||material.name().contains("BLACKSTONE") ||
                       material.name().equals("GRANITE") || material.name().equals("OBSIDIAN") ||
                        material.name().equals("DIORITE") || material.name().equals("ANDESITE") ||material.name().contains("COAL") || material.name().contains("DEEPSLATE")
                        ||material.name().contains("REDSTONE") ||material.name().contains("LAPIS") ||material.name().contains("DRIPSTONE") ||material.name().contains("IRON")
                        ||material.name().contains("DIAMOND")||material.name().contains("COPPER")||material.name().contains("GOLD")||material.name().contains("EMERALD")
                        ||material.name().contains("BRICKS")||material.name().contains("BRICK")||material.name().contains("BLOCK")) {
                    TOOL_ASSIGNMENTS.put(material, ToolType.PICKAXE);
                }
                // Axt: Holz und verwandte Materialien
                else if (material.name().contains("LOG") || material.name().contains("WOOD")|| material.name().contains("MANGROVE") ||
                        material.name().contains("PLANKS") ||material.name().contains("STEM") || material.name().contains("DOOR") ||
                        material.name().contains("CHEST") || material.name().contains("PUMPKIN") || material.name().contains("MELON")) {
                    TOOL_ASSIGNMENTS.put(material, ToolType.AXE);
                }
                // Schaufel: Erde, Sand und ähnliche Blöcke
                else if (material.name().contains("DIRT") || material.name().contains("SAND") ||material.name().contains("SOIL") ||
                        material.name().contains("GRASS") || material.name().contains("GRAVEL") ||
                        material.name().contains("SNOW") || material.name().contains("CLAY")) {
                    TOOL_ASSIGNMENTS.put(material, ToolType.SHOVEL);
                }
                // Schere: Blätter, Wolle und Teppiche
                else if (material.name().contains("LEAVES") || material.name().contains("VINE") ||
                        material.name().contains("WOOL") || material.name().contains("CARPET")|| material.name().contains("GLASS")) {
                    TOOL_ASSIGNMENTS.put(material, ToolType.SHEARS);
                }
                // HOE
                else if (material.name().contains("FARMLAND") || material.name().contains("CROP") ||
                        material.name().contains("BEETROOT") || material.name().contains("CARROT") ||
                        material.name().contains("POTATO") ||material.name().contains("LEAVES") ) {
                    TOOL_ASSIGNMENTS.put(material, ToolType.HOE);
                }
            }
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        ToolType toolType = TOOL_ASSIGNMENTS.getOrDefault(blockType, null);


        int bestSlot = -1;
        if (toolType != null) {
            bestSlot = findBestToolSlot(player, toolType);
        }

        //  beliebiges Werkzeug
        if (bestSlot == -1) {
            bestSlot = findAnyToolSlot(player);
        }


        if (bestSlot != -1 && bestSlot != player.getInventory().getHeldItemSlot()) {
            player.getInventory().setHeldItemSlot(bestSlot);
        }
    }

    private int findBestToolSlot(Player player, ToolType toolType) {
        int bestSlot = -1;
        int bestLevel = -1;

        for (int slot = 0; slot < 9; slot++) { // nur die Hotbar prüfen
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

    private int findAnyToolSlot(Player player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null) continue;

            if (isAnyTool(item.getType())) {
                return slot;
            }
        }
        return -1;
    }

    private boolean isAnyTool(Material mat) {
        return isToolType(mat, ToolType.PICKAXE) || isToolType(mat, ToolType.AXE) ||
                isToolType(mat, ToolType.SHOVEL) || isToolType(mat, ToolType.SHEARS) ||
                isToolType(mat, ToolType.HOE);
    }

    private boolean isToolType(Material mat, ToolType type) {
        switch (type) {
            case PICKAXE:
                return mat.name().endsWith("_PICKAXE");
            case AXE:
                return mat.name().endsWith("_AXE");
            case SHOVEL:
                return mat.name().endsWith("_SHOVEL");
            case SHEARS:
                return mat == Material.SHEARS;
            case HOE:
                return mat.name().endsWith("_HOE");
            default:
                return false;
        }
    }

    private int getToolLevel(Material mat) {
        if (mat.name().startsWith("WOODEN")) return 1;
        if (mat.name().startsWith("STONE")) return 2;
        if (mat.name().startsWith("IRON")) return 3;
        if (mat.name().startsWith("GOLDEN")) return 2; // schnell, aber geringe Haltbarkeit
        if (mat.name().startsWith("DIAMOND")) return 4;
        if (mat.name().startsWith("NETHERITE")) return 5;
        return 0;
    }

    enum ToolType {
        PICKAXE,
        AXE,
        SHOVEL,
        SHEARS,
        HOE
    }
}
