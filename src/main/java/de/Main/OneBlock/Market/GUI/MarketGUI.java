package de.Main.OneBlock.Market.GUI;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MarketGUI implements Listener, CommandExecutor {
    public static Inventory MarketGUI = Bukkit.createInventory(null, 36, "Market");
    private Economy eco;

    public MarketGUI(Economy economy) {
        this.eco = economy;
    }

    public static void createItems() {
        MarketGUI.setItem(35, new ItemStack(Material.LIME_DYE));
        MarketGUI.setItem(28, new ItemStack(Material.RED_DYE));
        MarketGUI.setItem(32, new ItemStack(Material.GOLD_INGOT));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            player.openInventory(MarketGUI);
        }
        return true;
    }
}
