package de.Main.OneBlock;

import de.Main.OneBlock.Market.GUI.MarketGUI;
import de.Main.OneBlock.Market.Listener.InventoryClick;
import de.Main.OneBlock.Market.Manager.MarketManager;
import de.Main.OneBlock.NPC.GUI.NPCGUI;
import de.Main.OneBlock.NPC.Listener.NPCInventoryListener;
import de.Main.OneBlock.NPC.Listener.NPCListener;
import de.Main.OneBlock.OneBlock.Manager.Manager;
import de.Main.OneBlock.OneBlock.Manager.OneBlockManager;
import de.Main.OneBlock.OneBlock.Player.PlayerListener;
import de.Main.OneBlock.OneBlock.Player.PlayerRespawnListener;
import de.Main.OneBlock.OneBlock.Player.ToolSwitch;
import de.Main.OneBlock.Quest.GUI.QuestMainGUI;
import de.Main.OneBlock.Quest.GUI.QuestRewardGUI;
import de.Main.OneBlock.Quest.Listener.QuestListener;
import de.Main.OneBlock.WorldManager.VoidGen;
import de.Main.OneBlock.WorldManager.WorldBorderManager;
import de.Main.OneBlock.database.DBM;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;


public class Main extends JavaPlugin implements Listener {
    private static Main instance;
    private static String prefix;
    public static final String WORLD_NAME = "OneBlock";
    public static World oneBlockWorld;
    public static FileConfiguration config;
    private static Economy economy = null;
    SQLConnection connection;
    DBM moneyManager;
    private File marketfile;
    private FileConfiguration marketconfig;
    public static File marketDataFolder;


    public static Main getInstance() {
        return instance;
    }


    @Override
    public void onEnable() {
        instance = this;
        //SQL
        connection = new SQLConnection("localhost", 3306, "admin", "admin", "1234");
        moneyManager = new DBM(this);

        //config
        setupMarketFile();
        saveDefaultConfig();
        config = getConfig();
        instance = this;
        if (!config.contains("maxlevel")) {
            config.set("maxlevel", 10);
        }
        marketfile = new File(getDataFolder(), "Market");
        if (!marketfile.exists()) {
            marketfile.mkdirs();
        }
        //Economy
        setupEconomy();
        if (economy != null) {
            Bukkit.getPluginManager().registerEvents(new Manager(economy, this), this);
            getLogger().info("Vault Economy erfolgreich erkannt.");
        } else {
            getLogger().warning("Vault wurde nicht gefunden – Economy wird deaktiviert.");
        }








        //<--------------------Welten Erstellung-------------------->>//
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





        //<--------------------NPC-------------------->>//
        Bukkit.getPluginManager().registerEvents(new NPCListener(), this);
        Bukkit.getPluginManager().registerEvents(new NPCInventoryListener(), this);
        NPCGUI.createNPCGUI();

        //<--------------------Quest-------------------->>//
        Bukkit.getPluginManager().registerEvents(new QuestListener(), this);
        QuestMainGUI.createQuestGUI();
        QuestRewardGUI.createQuestGUI();
        //<--------------------Market-------------------->>//
        Bukkit.getPluginManager().registerEvents(new MarketManager(economy, marketfile), this);
        Bukkit.getPluginManager().registerEvents(new MarketGUI(economy), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClick(),this);
        getCommand("market").setExecutor(new MarketGUI(economy));
        MarketGUI.createItems();
        setServerPrefix();

        //<--------------------OneBlock-------------------->>//
        OneBlockManager.startAutoSaveTask();
        //<-------------Listener-------------->>//
        getServer().getPluginManager().registerEvents(new de.Main.OneBlock.OneBlock.GUI.OneBlock.OBGUI(), this);
        Bukkit.getPluginManager().registerEvents(new ToolSwitch(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        //<-------------Commands-------------->>//
        getCommand("ob").setTabCompleter(new de.Main.OneBlock.OneBlock.Commands.TabCompleter());
        getCommand("ob").setExecutor(new de.Main.OneBlock.OneBlock.Commands.OneBlockCommands());
        getCommand("obgui").setExecutor(new de.Main.OneBlock.OneBlock.GUI.OneBlock.OBGUI());
        Bukkit.getPluginManager().registerEvents(new OneBlockManager(), this);
        //<-------------WorldBorder-------------->>//
        Bukkit.getPluginManager().registerEvents(new WorldBorderManager(), this);

        getLogger().info("OneBlockPlugin aktiviert!");
    }

    public static void setServerPrefix() {
        prefix = config.getString("Server");
    }

    public static String getPrefix() {
        return prefix;
    }

    @Override
    public void onDisable() {
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

    public void setupMarketFile() {
        marketfile = new File(getDataFolder(), "Market/market.yml");

        if (!marketfile.getParentFile().exists()) {
            marketfile.getParentFile().mkdirs();
        }
        if (!marketfile.exists()) {
            try {
                marketfile.createNewFile();
                getLogger().info("market.yml erstellt.");
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Konnte growth.yml nicht erstellen.", e);
            }
        }

        marketconfig = YamlConfiguration.loadConfiguration(marketfile);
    }

    public static List<UUID> getAllOwners() throws SQLException {
        List<UUID> owners = new ArrayList<>();
        String sql = "SELECT DISTINCT owner_uuid FROM userdata WHERE owner_uuid IS NOT NULL";


        Connection conn = Main.getInstance().getConnection().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String ownerUUIDStr = rs.getString("owner_uuid");
                if (ownerUUIDStr != null && !ownerUUIDStr.isEmpty()) {
                    try {
                        UUID ownerUUID = UUID.fromString(ownerUUIDStr);
                        owners.add(ownerUUID);
                    } catch (IllegalArgumentException e) {

                        Main.getInstance().getLogger().warning("Ungültige UUID in der Datenbank: " + ownerUUIDStr);
                    }
                }
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen der Besitzer aus der Datenbank", e);
        }
        return owners;
    }


}



