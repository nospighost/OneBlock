package de.Main.OneBlock.Market.GUI;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

public class MarketGUI implements Listener {
    private Economy eco;
    private FileConfiguration config;
    public MarketGUI(Economy economy, FileConfiguration marketconfig) {
        this.eco = eco;
        this.config = config;
    }
}
