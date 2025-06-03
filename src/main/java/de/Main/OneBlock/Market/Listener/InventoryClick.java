package de.Main.OneBlock.Market.Listener;

import de.Main.OneBlock.Market.GUI.MarketGUI;
import de.Main.OneBlock.Market.Manager.MarketManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryClick implements Listener {


    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        String title = event.getView().getTitle();
        ItemStack item = event.getCurrentItem();
        Material type = null;
        if (item != null) {
            type = item.getType();
        }
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        String inventoryTitle = player.getOpenInventory().getTitle();
        List<String> blockedInventories = Arrays.asList(
                "§cMarket"
        );



        if (blockedInventories.contains(inventoryTitle)) {

            if (clickedInventory.equals(topInventory)) {
                event.setCancelled(true);
            }
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                // Prüfe, ob der Zielinventar das Top-Inventar ist
                Inventory destinationInventory = event.getView().getTopInventory();

                // Nur blockieren, wenn Ziel das Top-Inventar ist
                if (destinationInventory.equals(topInventory)) {
                    event.setCancelled(true);
                }
            }
        }

        if (event.getCurrentItem() == null) {
            return;
        }
        if (title.equalsIgnoreCase("Market")) {


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
