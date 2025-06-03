package de.Main.OneBlock.Market.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MarketGUI implements CommandExecutor, Listener {

    public static Inventory inv = Bukkit.createInventory(null, 36, "Market");
    public static Inventory createMarketInventory() {

        ItemStack sell = new ItemStack(Material.LIME_DYE);
        ItemMeta sellMeta = sell.getItemMeta();
        sellMeta.setDisplayName("§aVerkaufen");
        sell.setItemMeta(sellMeta);
        inv.setItem(35, sell);

        ItemStack close = new ItemStack(Material.RED_DYE);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§cSchließen");
        close.setItemMeta(closeMeta);
        inv.setItem(27, close);

        ItemStack check = new ItemStack(Material.GOLD_INGOT);
        ItemMeta checkMeta = check.getItemMeta();
        checkMeta.setDisplayName("§6Preis anzeigen");
        check.setItemMeta(checkMeta);
        inv.setItem(31, check);

        return inv;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            player.openInventory((MarketGUI.inv));
            return true;
        }
        return false;
    }
}