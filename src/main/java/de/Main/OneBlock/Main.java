package de.Main.OneBlock;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.IOException;
import java.util.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

// NEU: Imports für Scoreboard
import org.bukkit.scoreboard.*;
import java.io.File;

public class Main extends JavaPlugin implements Listener {
    private static Main instance;

    private static final String WORLD_NAME = "OneBlock";
    public static World oneBlockWorld;

    public static FileConfiguration config;
    public static File islandDataFolder;
    private static Economy economy = null;

    private final Map<Location, ChestData> chests = new HashMap<>();
    private File dataFile;
    private YamlConfiguration dataConfig;

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
// --- NEU: Scoreboard Methoden ---

private void updateScoreboard(Player player) {
    ScoreboardManager manager = Bukkit.getScoreboardManager();
    if (manager == null) return;

    Scoreboard board = manager.getNewScoreboard();

    String title = getConfig().getString("scoreboard.title", "§6Scoreboard");
    Objective objective = board.registerNewObjective("sidebar", "dummy", colorize(title));
    objective.setDisplaySlot(DisplaySlot.SIDEBAR);

    List<String> lines = getConfig().getStringList("scoreboard.lines");
    int score = lines.size();

    for (String line : lines) {
        String replacedLine = replacePlaceholders(player, line);
        Score scoreLine = objective.getScore(colorize(replacedLine));
        scoreLine.setScore(score);
        score--;
    }

    player.setScoreboard(board);


private String replacePlaceholders(Player player, String text) {
    // Hier kannst du deine Platzhalter anpassen oder dynamisch aus deinem Plugin holen
    text = text.replace("%points%", "100"); // Beispielwert
    text = text.replace("%kills%", "5");    // Beispielwert
    text = text.replace("%deaths%", "2");   // Beispielwert
    return text;
}

private String colorize(String input) {
    return input.replace("&", "§");
}

static class ChestData {
    int level;
    List<ItemStack> contents = new ArrayList<>();

    ChestData(int level) {
        this.level = level;
    }
}
}

    