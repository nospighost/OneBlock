package de.Main.OneBlock.Market.Listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MarketCloseListener implements Listener {

    @EventHandler
    public void onMarketClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory closedInventory = event.getInventory();

        // Stelle sicher, dass es sich um das Market-GUI handelt
        if (event.getView().getTitle().equalsIgnoreCase("Market")) {

            for (int i = 0; i < closedInventory.getSize(); i++) {
                ItemStack item = closedInventory.getItem(i);
                if (i == 27 || i == 31 || i == 35) continue; // Buttons


                // Lege das Item zurück in das Spieler-Inventar
                player.getInventory().addItem(item);

                closedInventory.clear(i);

            }

            player.sendMessage("§eDeine Items wurden dir zurückgegeben.");
        }
    }
}
