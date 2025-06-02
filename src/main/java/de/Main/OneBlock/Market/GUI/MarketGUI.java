package de.Main.OneBlock.Market.GUI;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MarketGUI implements Listener, CommandExecutor {

    public static Inventory MarketGUI = Bukkit.createInventory(null, 36, "Market");

    private Economy eco;
    private FileConfiguration config;
    public MarketGUI(Economy economy, FileConfiguration marketconfig) {
        this.eco = eco;
        this.config = config;
    }


    public static void createItems() {

        ItemStack sell = new ItemStack(Material.GREEN_DYE);

        MarketGUI.setItem(35, sell);

        ItemStack close = new ItemStack(Material.RED_DYE);

        MarketGUI.setItem(28, close);


        ItemStack getPrice = new ItemStack(Material.GOLD_INGOT);

        MarketGUI.setItem(32, getPrice);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            player.openInventory(MarketGUI);
        }
        return false;
    }
}
