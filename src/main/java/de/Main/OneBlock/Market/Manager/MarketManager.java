package de.Main.OneBlock.Market.Manager;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

public class MarketManager implements Listener {
    private static Economy eco;
    private static FileConfiguration config;
    public MarketManager(Economy eco, FileConfiguration config) {
        this.eco = eco;
        this.config = config;
    }

    public static void sellAllItems(Inventory inv) {
        Player player = (Player) inv.getHolder();

        for (int i = 0; i <27; i++) {

            Material item = inv.getItem(i).getType();
            int amount = inv.getItem(i).getAmount();
            int price = config.getInt(String.valueOf(item));
            int finalprice = price * amount;
            eco.depositPlayer(player, finalprice);
            inv.setItem(i, null);
        }
    }

    public static double getSellPrice(Inventory inv) {
        Player player = (Player) inv.getHolder();
        Integer finalprice = 0;

        for (int i = 0; i <27; i++) {

            Material item = inv.getItem(i).getType();
            int amount = inv.getItem(i).getAmount();
            int price = config.getInt(String.valueOf(item));
             finalprice = price * amount;
            eco.depositPlayer(player, finalprice);
            inv.setItem(i, null);
            return finalprice;
        }
        return finalprice;
    }


}
