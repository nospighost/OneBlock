package de.Main.OneBlock;

import com.sk89q.worldguard.WorldGuard;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {
    private static Main instance; // <- Hier

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


        //config
        saveDefaultConfig();
        config = getConfig();
        instance = this;
        if (!config.contains("value")) {
            config.set("value", 400);
            saveConfig();
        }


        setupEconomy();


        // Listener registrieren
       Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        if (economy != null) {
            Bukkit.getPluginManager().registerEvents(new Manager(economy, this), this);
            getLogger().info("Vault Economy erfolgreich erkannt.");
        } else {
            getLogger().warning("Vault wurde nicht gefunden – Economy wird deaktiviert.");
        }

        getCommand("ob").setTabCompleter(new TabCompleter());



        getLogger().info("OneBlockPlugin aktiviert!");

        // Ordner Erstellen//
        islandDataFolder = new File(getDataFolder(), "IslandData");
        if (!islandDataFolder.exists()) {
            islandDataFolder.mkdirs();
        }

        // Befehle
        getCommand("ob").setExecutor(new de.Main.OneBlock.OneBlockCommands());
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

        for (UUID playerName : Manager.getAllIslandOwners()) {
            File file = new File(islandDataFolder, playerName + ".yml");
            if (file.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                int x = config.getInt("OneBlock-x");
                int z = config.getInt("OneBlock-z");
                int size = config.getInt("WorldBorderSize", 50);


                Player player = Bukkit.getPlayerExact(playerName.toString());
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

}
