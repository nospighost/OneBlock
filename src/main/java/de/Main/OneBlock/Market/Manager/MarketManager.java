package de.Main.OneBlock.Market.Manager;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MarketManager implements Listener {

    private static  Economy economy;
    private static  FileConfiguration sellPriceConfig;

    public MarketManager(Economy economy, FileConfiguration sellPriceConfig) {
        this.economy = economy;
        this.sellPriceConfig = sellPriceConfig;
    }

    public static void sellAllItems(Inventory inv, Player player) {
        double total = 0;

        for (int i = 0; i < 27; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;

            String key = item.getType().name().toLowerCase();
            if (!sellPriceConfig.contains("prices." + key)) continue;

            double price = sellPriceConfig.getDouble("prices." + key);
            total += price * item.getAmount();
            inv.setItem(i, null);
        }

        if (total > 0) {
            economy.depositPlayer(player, total);
            player.sendMessage("§aDu hast Items im Wert von §e" + total + "§a verkauft.");
        } else {
            player.sendMessage("§cKeine Items mit Verkaufswert gefunden.");
        }
    }

    public static double getSellPrice(Inventory inv) {
        double total = 0;

        for (ItemStack item : inv.getContents()) {
            if (item == null) continue;

            String key = item.getType().name().toLowerCase();
            if (!sellPriceConfig.contains("prices." + key)) continue;

            double price = sellPriceConfig.getDouble("prices." + key);
            total += price * item.getAmount();
        }

        return total;
    }
}
