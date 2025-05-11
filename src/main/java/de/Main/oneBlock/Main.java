package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin implements Listener {

    private static final String WORLD_NAME = "OneBlock";
    private World oneBlockWorld;

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


            WorldBorder border = oneBlockWorld.getWorldBorder();
            Integer defaultsize = Main.config.getInt("defaultworldbordersize.size");
            border.setCenter(0.0, 0.0);
            border.setSize(defaultsize);
            border.setDamageBuffer(0);
            border.setDamageAmount(0.5);
            border.setWarningDistance(5);
            border.setWarningTime(15);
        } else {
            getLogger().warning("Fehler beim Erstellen der OneBlock-Welt.");
        }

        setInitialOneBlock();


    }

    @Override
    public void onDisable() {
        getLogger().info("OneBlockPlugin deaktiviert.");
    }

    private void setInitialOneBlock() {
        if (oneBlockWorld != null) {
            Location blockLocation = new Location(oneBlockWorld, 0, 100, 0);
            oneBlockWorld.getBlockAt(blockLocation).setType(Material.STONE);
        }
    }
}
