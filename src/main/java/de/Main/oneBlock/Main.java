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
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin implements Listener {

    private static final String WORLD_NAME = "OneBlock";
    private static World oneBlockWorld;

    public static FileConfiguration config;
    public static File islandDataFolder;

    @Override
    public void onEnable() {
        //config
        saveDefaultConfig();
        config = getConfig();

        // Listener registrieren
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        getLogger().info("OneBlockPlugin aktiviert!");
        //Ordner Erstellen//
        islandDataFolder = new File(getDataFolder(), "IslandData");
        if (!islandDataFolder.exists()) {
            islandDataFolder.mkdirs();
        }


        //Befehle
        getCommand("ob").setExecutor(new OneBlockCommands());

        // Void Gen für OneBlock-Welt
        WorldCreator worldCreator = new WorldCreator(WORLD_NAME);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.type(WorldType.FLAT);
        worldCreator.generator(new VoidGen());

        oneBlockWorld = Bukkit.createWorld(worldCreator);

        if (oneBlockWorld != null) {
            getLogger().info("OneBlock-Welt wurde erfolgreich erstellt!");
            oneBlockWorld.setSpawnLocation(0, 100, 0);

        } else {
            getLogger().warning("Fehler beim Erstellen der OneBlock-Welt.");
        }

    }

    @Override
    public void onDisable() {
        saveDefaultConfig();
        getLogger().info("OneBlockPlugin deaktiviert.");
    }


    public static void setWorldBorder(Player player) {
        YamlConfiguration config = Manager.getIslandConfig(player);
        if (oneBlockWorld != null) {
            Location blockLocation = new Location(oneBlockWorld, 0, 100, 0);
            oneBlockWorld.getBlockAt(blockLocation).setType(Material.STONE);
        }
        WorldBorder border = oneBlockWorld.getWorldBorder();
        Integer defaultsize = config.getInt("WorldBorderSize");
        border.setCenter(config.getInt("OneBlock-x"), config.getInt("OneBlock-z"));
        border.setSize(defaultsize);
        border.setDamageBuffer(0);
        border.setDamageAmount(0.5);
        border.setWarningDistance(5);
        border.setWarningTime(15);
    }
}
