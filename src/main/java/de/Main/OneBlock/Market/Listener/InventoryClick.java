package de.Main.OneBlock.Market.Listener;

import de.Main.OneBlock.Market.Manager.MarketManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryClick implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (!event.getView().getTitle().equalsIgnoreCase("Market")) return;
        event.setCancelled(true);

        switch (item.getType()) {
            case LIME_DYE -> MarketManager.sellAllItems(event.getView().getTopInventory());
            case RED_DYE -> player.closeInventory();
            case GOLD_INGOT -> player.sendMessage("§aDu bekommst: §6" + MarketManager.getSellPrice(event.getView().getTopInventory()));
        }
    }
}