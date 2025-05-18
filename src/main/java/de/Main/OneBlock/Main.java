package de.Main.OneBlock;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import org.bukkit.event.Listener;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import java.util.UUID;

public class Main extends JavaPlugin implements Listener {
    private static Main instance;

    private static final String WORLD_NAME = "OneBlock";
    public static World oneBlockWorld;

    public static FileConfiguration config;
    public static File islandDataFolder;


    public static Main getInstance() {
        return instance;
    }

    private static Economy economy = null;

    @Override
    public void onEnable() {

        // Config laden
        saveDefaultConfig();
        config = getConfig();
        instance = this;

        setupEconomy();

        // Listener registrieren
        Bukkit.getPluginManager().registerEvents(new WorldBorderManager(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        if (economy != null) {
            Bukkit.getPluginManager().registerEvents(new Manager(economy, this), this);
            getLogger().info("Vault Economy erfolgreich erkannt.");
        } else {
            getLogger().warning("Vault wurde nicht gefunden – Economy wird deaktiviert.");
        }

        // Tab-Completion
        getCommand("ob").setTabCompleter(new TabCompleter());

        getLogger().info("OneBlockPlugin aktiviert!");

        // Insel-Daten-Ordner erstellen
        islandDataFolder = new File(getDataFolder(), "IslandData");
        if (!islandDataFolder.exists()) {
            islandDataFolder.mkdirs();
        }

        // Befehle
        getCommand("ob").setExecutor(new de.Main.OneBlock.OneBlockCommands());
        getCommand("obgui").setExecutor(new OBGUI());
        getServer().getPluginManager().registerEvents(new OBGUI(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(islandDataFolder), this);

        // OneBlock-Welt erzeugen (Void-Gen)
        WorldCreator worldCreator = new WorldCreator(WORLD_NAME);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.type(WorldType.FLAT);
        worldCreator.generator(new VoidGen());

        oneBlockWorld = Bukkit.createWorld(worldCreator);

        WorldBorder border = oneBlockWorld.getWorldBorder();

             border.setCenter(0, 0);
             border.setSize(9999999);
             border.setDamageBuffer(0);
             border.setDamageAmount(0.5);
             border.setWarningDistance(5);
             border.setWarningTime(15);


    }

    @Override
    public void onDisable() {

        Manager.saveIslandConfig(null, null);
        saveDefaultConfig();
        getLogger().info("OneBlockPlugin deaktiviert.");

    }


    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
        return economy != null;
    }

    public static Economy getEconomy() {
        return economy;
    }

}