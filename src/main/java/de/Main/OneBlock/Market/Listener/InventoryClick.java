package de.Main.OneBlock.Market.Listener;

import de.Main.OneBlock.Market.Manager.MarketManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClick implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Market")) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        Material type = clicked.getType();
        Player player = (Player) event.getWhoClicked();

        if (type == Material.LIME_DYE) {
            event.setCancelled(true);
            MarketManager.sellAllItems(event.getInventory(), player);
            player.sendMessage("§aAlle Items wurden verkauft!");
        } else if (type == Material.GOLD_INGOT) {
            event.setCancelled(true);
            double value = MarketManager.getSellPrice(event.getInventory());
            player.sendMessage("§6Gesamtwert: §a" + value);
        } else if (type == Material.RED_DYE) {
            event.setCancelled(true);
            player.closeInventory();
        }
    }
}