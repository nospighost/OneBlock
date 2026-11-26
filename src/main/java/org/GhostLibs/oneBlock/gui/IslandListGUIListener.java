package org.GhostLibs.oneBlock.gui;

import org.GhostLibs.oneBlock.OneBlock;
import org.GhostLibs.oneBlock.manager.OBManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class IslandListGUIListener implements Listener {

    private final NamespacedKey islandKey = new NamespacedKey(OneBlock.getInstance(), "island_item");

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof IslandListGUI)) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getInventory())) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        PersistentDataContainer container = clickedItem.getItemMeta().getPersistentDataContainer();
        if (container.has(islandKey, PersistentDataType.INTEGER)) {
            int playerId = container.get(islandKey, PersistentDataType.INTEGER);
            Player player = (Player) event.getWhoClicked();
            OBManager.getInstance().teleportToOneblock(playerId, player);
            player.closeInventory();


        }
    }
}
