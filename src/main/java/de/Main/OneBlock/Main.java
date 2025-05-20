package de.Main.OneBlock;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;


public class Main extends JavaPlugin implements Listener {
    private static Main instance;

    private static final String WORLD_NAME = "OneBlock";
    public static World oneBlockWorld;

    public static FileConfiguration config;
    public static File islandDataFolder;
    public static File GenDataFolder;
    private static Economy economy = null;
    public File CustomItems;

    public static Main getInstance() {
        return instance;
    }


    @Override
    public void onEnable() {
        instance = this;


        //config
        saveDefaultConfig();
        config = getConfig();
        instance = this;


        setupEconomy();
        OBItems.createCustomItemsConfig(this);

        // Listener registrieren
        OBItems obItems = new OBItems(this);
        getServer().getPluginManager().registerEvents(obItems, this);
        getCommand("globaltrash").setExecutor(obItems);
        obItems.start();


        // Bukkit.getPluginManager().registerEvents(new Generator(this), this);

        Bukkit.getPluginManager().registerEvents(new Test(), this);


        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        if (economy != null) {
            Bukkit.getPluginManager().registerEvents(new Manager(economy, this), this);
            getLogger().info("Vault Economy erfolgreich erkannt.");
        } else {
            getLogger().warning("Vault wurde nicht gefunden – Economy wird deaktiviert.");
        }
        Bukkit.getPluginManager().registerEvents(new WorldBorderManager(), this);
        getCommand("ob").setTabCompleter(new TabCompleter());


        getLogger().info("OneBlockPlugin aktiviert!");

        // Ordner Erstellen//
        islandDataFolder = new File(getDataFolder(), "IslandData");
        if (!islandDataFolder.exists()) {
            islandDataFolder.mkdirs();
        }
        GenDataFolder = new File(getDataFolder(), "GenDataFolder");
        if (!GenDataFolder.exists()) {
            GenDataFolder.mkdirs();
        } else {
            getLogger().info("GenDataFolder wurde NICHT erfolgreich erstellt!");
        }
        CustomItems = new File(getDataFolder(), "CustomItems");
        if (!CustomItems.exists()) {
            CustomItems.mkdirs();
        } else {
            getLogger().info("CustomItems folder wurde nicht  erstellt!");
        }


        // Befehle
        getCommand("ob").setExecutor(new de.Main.OneBlock.OneBlockCommands());
        getCommand("obgui").setExecutor(new OBGUI());
        getCommand("globaltrash").setExecutor(new OBItems(this));

        getServer().getPluginManager().registerEvents(new OBGUI(), this);

        // Void Gen für OneBlock-Weltaa
        WorldCreator worldCreator = new WorldCreator(WORLD_NAME);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.type(WorldType.FLAT);
        worldCreator.generator(new VoidGen());

        oneBlockWorld = Bukkit.createWorld(worldCreator);

        if (oneBlockWorld != null) {
            getLogger().info("OneBlock-Welt wurde erfolgreich erstellt!");
            oneBlockWorld.setSpawnLocation(0, 100, 0);

            WorldBorder border = Bukkit.createWorldBorder();
            border.setCenter(0, 0);
            border.setSize(100000);
            border.setDamageBuffer(0);
            border.setDamageAmount(0.5);
            border.setWarningDistance(5);
            border.setWarningTime(15);

        } else {
            getLogger().warning("Fehler beim Erstellen der OneBlock-Welt");
        }


    }

    @Override
    public void onDisable() {

        Manager.saveIslandConfig(null, null);
        saveDefaultConfig();

        getLogger().info("OneBlockPlugin deaktiviert.");

    }


    public static void setWorldBorder(Player player) {
        YamlConfiguration config = Manager.getIslandConfig(player.getUniqueId());
        int x = config.getInt("OneBlock-x");
        int z = config.getInt("OneBlock-z");
        int size = config.getInt("WorldBorderSize");
        WorldBorder border = player.getWorld().getWorldBorder();
        border.setCenter(x, z);
        border.setSize(size);
        border.setDamageBuffer(0);
        border.setDamageAmount(0.5);
        border.setWarningDistance(5);
        border.setWarningTime(15);

        player.setWorldBorder(border);

        if (oneBlockWorld != null) {
            Location blockLocation = new Location(oneBlockWorld, x, 100, z);
            if (blockLocation.getBlock().getType() == Material.AIR) {
                oneBlockWorld.setType(blockLocation, Material.OAK_LOG);
            } else {
                oneBlockWorld.getBlockAt(blockLocation).setType(blockLocation.getBlock().getType());
            }

        }
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


    public static Plugin getPlugin() {
        return JavaPlugin.getPlugin(Main.class);
    }


}



