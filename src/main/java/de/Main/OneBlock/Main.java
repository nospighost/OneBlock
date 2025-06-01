package de.Main.OneBlock;

import de.Main.OneBlock.CustomChest.ChestListener;
import de.Main.OneBlock.Kristalle.GUI.KristallGUI;
import de.Main.OneBlock.Kristalle.GUI.PickaxeShop.PickaxeShop;
import de.Main.OneBlock.Kristalle.Listener.GrowthManager;
import de.Main.OneBlock.Kristalle.Listener.PlayerListener;
import de.Main.OneBlock.OneBlock.Manager.Manager;
import de.Main.OneBlock.OneBlock.Manager.OneBlockManager;
import de.Main.OneBlock.OneBlock.Player.PlayerRespawnListener;
import de.Main.OneBlock.WorldManager.VoidGen;
import de.Main.OneBlock.WorldManager.WorldBorderManager;
import de.Main.OneBlock.database.DatenBankManager;
import de.Main.OneBlock.database.SQLConnection;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
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

public class Main extends JavaPlugin {
    private static Main instance;

    public static final String WORLD_NAME = "OneBlock";
    public static World oneBlockWorld;

    private static Economy economy = null;
    public static FileConfiguration config;
    public static File islandDataFolder;

    private File growthFile;
    private FileConfiguration growthConfig;

    private SQLConnection connection;
    private DatenBankManager moneyManager;

    private GrowthManager growthManager;

    public static Main getInstance() {
        return instance;
    }

    public SQLConnection getConnection() {
        return connection;
    }

    @Override
    public void onEnable() {
        instance = this;

        // SQL-Verbindung initialisieren
        connection = new SQLConnection("localhost", 3306, "admin", "admin", "1234");
        moneyManager = new DatenBankManager(this);

        // Config laden und default setzen
        saveDefaultConfig();
        config = getConfig();

        if (!config.contains("maxlevel")) {
            config.set("maxlevel", 10);
            saveConfig();
        }

        // Economy Setup
        if (!setupEconomy()) {
            getLogger().warning("Vault wurde nicht gefunden – Economy wird deaktiviert.");
        } else {
            getLogger().info("Vault Economy erfolgreich erkannt.");
        }

        // Listener registrieren
        Bukkit.getPluginManager().registerEvents(new PlayerRespawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this, growthManager, economy), this);

        if (economy != null) {
            Bukkit.getPluginManager().registerEvents(new Manager(economy, this), this);
        }

        Bukkit.getPluginManager().registerEvents(new WorldBorderManager(), this);
        Bukkit.getPluginManager().registerEvents(new OneBlockManager(), this);
        Bukkit.getPluginManager().registerEvents(new ChestListener(), this);

        // Commands & TabCompleter
        getCommand("ob").setExecutor(new de.Main.OneBlock.OneBlock.Commands.OneBlockCommands());
        getCommand("ob").setTabCompleter(new de.Main.OneBlock.OneBlock.Commands.TabCompleter());

        getCommand("obgui").setExecutor(new de.Main.OneBlock.OneBlock.GUI.OneBlock.OBGUI());
        Bukkit.getPluginManager().registerEvents(new de.Main.OneBlock.OneBlock.GUI.OneBlock.OBGUI(), this);

        getCommand("pickaxeshop").setExecutor(new PickaxeShop());
        Bukkit.getPluginManager().registerEvents(new PickaxeShop(), this);

        getCommand("kristallshop").setExecutor(new KristallGUI());
        Bukkit.getPluginManager().registerEvents(new KristallGUI(), this);

        // Ordner für Insel-Daten erstellen
        islandDataFolder = new File(getDataFolder(), "IslandData");
        if (!islandDataFolder.exists()) {
            islandDataFolder.mkdirs();
        }

        // Growth File Setup (für Kristall-Wachstum)
        setupGrowthFile();

        // OneBlock-Welt erzeugen mit Void Generator
        createOneBlockWorld();

        // Kristall Listener & Task starten
// Nach setupGrowthFile() in onEnable:
        growthManager = new GrowthManager(growthConfig, growthFile);  // Beispielkonstruktor anpassen!

// Dann:
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this, growthManager, economy), this);
        growthManager.startGrowthTasks(this, growthConfig);  // wenn startGrowthTasks() nicht statisch ist


        getLogger().info("OneBlockPlugin aktiviert!");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("OneBlockPlugin deaktiviert.");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
        return economy != null;
    }

    private void setupGrowthFile() {
        growthFile = new File(getDataFolder(), "growth/growth.yml");

        if (!growthFile.getParentFile().exists()) {
            growthFile.getParentFile().mkdirs();
        }
        if (!growthFile.exists()) {
            try {
                growthFile.createNewFile();
                getLogger().info("growth.yml wurde erstellt.");
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Konnte growth.yml nicht erstellen.", e);
            }
        }

        growthConfig = YamlConfiguration.loadConfiguration(growthFile);
    }

    private void createOneBlockWorld() {
        WorldCreator worldCreator = new WorldCreator(WORLD_NAME);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.type(WorldType.FLAT);
        worldCreator.generator(new VoidGen());

        oneBlockWorld = Bukkit.createWorld(worldCreator);

        if (oneBlockWorld != null) {
            getLogger().info("OneBlock-Welt wurde erfolgreich erstellt!");
            oneBlockWorld.setSpawnLocation(0, 100, 0);

            WorldBorder border = oneBlockWorld.getWorldBorder();
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

    /**
     * Liefert alle Besitzer-UUIDs aus der Datenbank zurück.
     * Verbindung bleibt offen, PreparedStatement und ResultSet werden geschlossen.
     */
    public static List<UUID> getAllOwners() {
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

    public static Plugin getPlugin() {
        return JavaPlugin.getPlugin(Main.class);
    }

    public static Economy getEconomy() {
        return economy;
    }
}
