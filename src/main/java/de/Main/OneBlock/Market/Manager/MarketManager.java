package de.Main.OneBlock.Market.Manager;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class MarketManager implements Listener {
    private static Economy eco;
    private static FileConfiguration sellPrices;

    public MarketManager(Economy eco, File dataFolder) {
        MarketManager.eco = eco;
        File sellFile = new File(dataFolder, "sell-prices.yml");
        sellPrices = YamlConfiguration.loadConfiguration(sellFile);
    }

    public static void sellAllItems(Inventory inv) {
        Player player = (Player) inv.getHolder();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == null) continue;
            Material item = stack.getType();
            int amount = stack.getAmount();
            String key = item.name().toLowerCase();
            if (!sellPrices.contains("prices." + key)) continue;
            double price = sellPrices.getDouble("prices." + key);
            eco.depositPlayer(player, price * amount);
            inv.setItem(i, null);
        }
    }

    public static double getSellPrice(Inventory inv) {
        double total = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == null) continue;
            Material item = stack.getType();
            int amount = stack.getAmount();
            String key = item.name().toLowerCase();
            if (!sellPrices.contains("prices." + key)) continue;
            double price = sellPrices.getDouble("prices." + key);
            total += price * amount;
        }
        return total;
    }
}