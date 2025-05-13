package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin implements Listener {
    private static Main instance; // <- Hier

    private static final String WORLD_NAME = "OneBlock";
    public static World oneBlockWorld;

    public static FileConfiguration config;
    public static File islandDataFolder;

    public static Main getInstance() {
        return instance;
    }


    @Override
    public void onEnable() {
        //config
        saveDefaultConfig();
        config = getConfig();
        instance = this;
        if (!config.contains("value")) {
            config.set("value", 400);
            saveConfig();
        }
        // Listener registrieren
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        getCommand("ob").setTabCompleter(new TabCompleter());
        getLogger().info("OneBlockPlugin aktiviert!");

        // Ordner Erstellen//
        islandDataFolder = new File(getDataFolder(), "IslandData");
        if (!islandDataFolder.exists()) {
            islandDataFolder.mkdirs();
        }

        // Befehle
        getCommand("ob").setExecutor(new OneBlockCommands());
        getCommand("obgui").setExecutor(new OBGUI());
        getServer().getPluginManager().registerEvents(new OBGUI(), this);

        // Void Gen für OneBlock-Welt
        WorldCreator worldCreator = new WorldCreator(WORLD_NAME);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.type(WorldType.FLAT);
        worldCreator.generator(new VoidGen());

        oneBlockWorld = Bukkit.createWorld(worldCreator);

        if (oneBlockWorld != null) {
            getLogger().info("OneBlock-Welt wurde erfolgreich erstellt!");
            oneBlockWorld.setSpawnLocation(0, 100, 0);

            // Setze die Border für die gesamte Welt
            WorldBorder worldBorder = oneBlockWorld.getWorldBorder();
            worldBorder.setCenter(0, 0);
            worldBorder.setSize(50);
            worldBorder.setDamageBuffer(0);
            worldBorder.setDamageAmount(0.5);
            worldBorder.setWarningDistance(5);
            worldBorder.setWarningTime(15);
        } else {
            getLogger().warning("Fehler beim Erstellen der OneBlock-Welt");
        }

        // Hol alle Inselbesitzer und setze deren Border
        for (String playerName : Manager.getAllIslandOwners()) {
            File file = new File(islandDataFolder, playerName + ".yml");
            if (file.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                int x = config.getInt("OneBlock-x");
                int z = config.getInt("OneBlock-z");
                int size = config.getInt("WorldBorderSize", 50);

                // Border für den Spieler setzen
                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null && player.isOnline()) {
                    WorldBorder border = Bukkit.createWorldBorder();
                    border.setCenter(x, z);
                    border.setSize(size);
                    border.setDamageBuffer(0);
                    border.setDamageAmount(0.5);
                    border.setWarningDistance(5);
                    border.setWarningTime(15);

                    player.setWorldBorder(border);
                }
            }
        }
    }


    @Override
    public void onDisable() {

        Manager.saveIslandConfig(null, null);
        saveDefaultConfig();
        getLogger().info("OneBlockPlugin deaktiviert.");
    }


    public static void setWorldBorder(Player player) {
        YamlConfiguration config = Manager.getIslandConfig(player);
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
        // Optional: OneBlock wieder setzen
        if (oneBlockWorld != null) {
            Location blockLocation = new Location(oneBlockWorld, x, 100, z);
            if (blockLocation.getBlock().getType() == Material.AIR) {
                oneBlockWorld.setType(blockLocation, Material.OAK_LOG);
            } else {
                oneBlockWorld.getBlockAt(blockLocation).setType(blockLocation.getBlock().getType());
            }

        }
    }
}
