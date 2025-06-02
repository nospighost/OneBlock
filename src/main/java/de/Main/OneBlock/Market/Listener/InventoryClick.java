package de.Main.OneBlock.Market.Listener;

import de.Main.OneBlock.Market.GUI.MarketGUI;
import de.Main.OneBlock.Market.Manager.MarketManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryClick implements Listener {


    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        String title = event.getView().getTitle();
        ItemStack item = event.getCurrentItem();
        Material type = item.getType();

        if (event.getCurrentItem() == null) {
            return;
        }
        if (title.equalsIgnoreCase("Market")) {
           event.setCancelled(true);

            switch (type) {
                case GREEN_DYE -> {
                    MarketManager.sellAllItems(event.getView().getTopInventory());
                }
                case RED_DYE -> {
                    player.closeInventory();
                }
                case GOLD_INGOT -> {
                    player.sendMessage("TEST");
                }
            }
        }
    }
}
