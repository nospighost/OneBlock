package de.Main.OneBlock;

import de.Main.OneBlock.Kristalle.GUI.KristallGUI;
import de.Main.OneBlock.Kristalle.GUI.PickaxeShop.PickaxeShop;
import de.Main.OneBlock.Manager.Manager;
import de.Main.OneBlock.OneBlock.Player.OneBlockManager;
import de.Main.OneBlock.Player.PlayerListener;
import de.Main.OneBlock.WorldManager.VoidGen;
import de.Main.OneBlock.database.MoneyManager;
import de.Main.OneBlock.database.SQLConnection;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;


public class Main extends JavaPlugin implements Listener {
    private static Main instance;

    private static final String WORLD_NAME = "OneBlock";
    public static World oneBlockWorld;

    public static FileConfiguration config;
    public static File islandDataFolder;
    public static File GenDataFolder;
    private static Economy economy = null;
    public File CustomItems;
    SQLConnection connection;
    MoneyManager moneyManager;
    private File growthFile;
    private FileConfiguration growthConfig;

    public static Main getInstance() {
        return instance;
    }


    @Override
    public void onEnable() {
        instance = this;

        //SQL
        connection = new SQLConnection("localhost", 3306, "admin", "admin", "1234");
        moneyManager = new MoneyManager(this);

        //config
        saveDefaultConfig();
        config = getConfig();
        instance = this;
        if (!config.contains("maxlevel")) {
            config.set("maxlevel", 10);
        }
        setupEconomy();

        //Listener
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        if (economy != null) {
            Bukkit.getPluginManager().registerEvents(new Manager(economy, this), this);
            getLogger().info("Vault Economy erfolgreich erkannt.");
        } else {
            getLogger().warning("Vault wurde nicht gefunden – Economy wird deaktiviert.");
        }
      //  Bukkit.getPluginManager().registerEvents(new WorldBorderManager(), this); //rrrrrrrrrrrrrrrrrrrrrrrrrrrrr
        getCommand("ob").setTabCompleter(new de.Main.OneBlock.OneBlock.Commands.TabCompleter());
        Bukkit.getPluginManager().registerEvents(new OneBlockManager(), this);

        getLogger().info("OneBlockPlugin aktiviert!");

        // Ordner Erstellen//
        islandDataFolder = new File(getDataFolder(), "IslandData");
        if (!islandDataFolder.exists()) {
            islandDataFolder.mkdirs();
        }

        // Befehle
        getCommand("ob").setExecutor(new de.Main.OneBlock.OneBlock.Commands.OneBlockCommands());
        getCommand("obgui").setExecutor(new de.Main.OneBlock.OneBlock.GUI.OneBlock.OBGUI());

        getServer().getPluginManager().registerEvents(new de.Main.OneBlock.OneBlock.GUI.OneBlock.OBGUI(), this);

        // Void Gen für OneBlock-Welt
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

        //Kristall
        setupEconomy();
        setupGrowthFile();
        getServer().getPluginManager().registerEvents(new de.Main.OneBlock.Kristalle.Listener.PlayerListener(this, growthConfig, growthFile), this);
        de.Main.OneBlock.Kristalle.Listener.PlayerListener.startGrowthTasks(this, growthConfig);
        //Commands
        getCommand("pickaxeshop").setExecutor(new PickaxeShop());
        Bukkit.getPluginManager().registerEvents(new PickaxeShop(), this);
        getCommand("kristallshop").setExecutor(new KristallGUI());
        //Listener
        Bukkit.getPluginManager().registerEvents(new KristallGUI(), this);

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


    public static Plugin getPlugin() {
        return JavaPlugin.getPlugin(Main.class);
    }

    public SQLConnection getConnection() {
        return connection;
    }

    public void setupGrowthFile() {
        growthFile = new File(getDataFolder(), "growth/growth.yml");

        if (!growthFile.getParentFile().exists()) {
            growthFile.getParentFile().mkdirs();
        }
        if (!growthFile.exists()) {
            try {
                growthFile.createNewFile();
                getLogger().info("growth.yml erstellt.");
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Konnte growth.yml nicht erstellen.", e);
            }
        }

        growthConfig = YamlConfiguration.loadConfiguration(growthFile);
    }
}



