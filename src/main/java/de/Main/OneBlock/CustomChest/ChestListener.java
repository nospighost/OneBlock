package de.Main.OneBlock.CustomChest;

import de.Main.OneBlock.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class ChestListener implements Listener {

    private final NamespacedKey customChestKey = new NamespacedKey(Main.getInstance(), "CustomChest");
    private final Map<Location, Inventory> advancedChestInventories = new HashMap<>();

    // BlockPlaceEvent - Markiere eine Chest als "CustomChest", wenn sie spezifische Kriterien erfüllt
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.CHEST) return;

        ItemMeta itemMeta = event.getItemInHand().getItemMeta();
        if (itemMeta != null && "CustomChest".equals(itemMeta.getDisplayName())) {
            Block block = event.getBlockPlaced();
            Chest chest = (Chest) block.getState();
            PersistentDataContainer data = chest.getPersistentDataContainer();
            data.set(customChestKey, PersistentDataType.STRING, "true");
            chest.update();
        }
    }

    // PlayerInteractEvent - Interagiere nur mit CustomChests
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (isAdvancedChest(block)) {
            Inventory inv = getInventoryForChest(block.getLocation());
            event.getPlayer().openInventory(inv);
            event.setCancelled(true);
        }
    }


    private Inventory getInventoryForChest(Location loc) {
        if (!advancedChestInventories.containsKey(loc)) {
            Inventory newInv = Bukkit.createInventory(null, 54, "CustomChest");
            advancedChestInventories.put(loc, newInv);
        }
        return advancedChestInventories.get(loc);
    }

    // Prüfe, ob ein Block eine CustomChest ist
    private boolean isAdvancedChest(Block block) {
        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            PersistentDataContainer data = chest.getPersistentDataContainer();
            return "true".equals(data.get(customChestKey, PersistentDataType.STRING));
        }
        return false;
    }
}
