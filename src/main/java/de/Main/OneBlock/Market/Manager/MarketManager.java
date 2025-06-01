package de.Main.OneBlock.Market.Manager;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

public class MarketManager implements Listener {
     private Economy eco;
     private FileConfiguration config;
    public MarketManager(Economy eco, FileConfiguration config) {
        this.eco = eco;
        this.config = config;
    }


}
