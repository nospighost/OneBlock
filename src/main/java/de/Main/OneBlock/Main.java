package de.Main.OneBlock;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
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
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
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

        if (oneBlockWorld != null) {
            getLogger().info("OneBlock-Welt wurde erfolgreich erstellt!");
            oneBlockWorld.setSpawnLocation(0, 100, 0);

            // Setze eine globale WorldBorder für die Welt
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

        // WorldBorders für alle existierenden Inseln setzen
        for (UUID uuid : Manager.getAllIslandOwners()) {
            File file = new File(islandDataFolder, uuid + ".yml");
            if (file.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                int x = config.getInt("OneBlock-x");
                int z = config.getInt("OneBlock-z");
                int size = config.getInt("WorldBorderSize", 50);

                WorldBorder border = Bukkit.createWorldBorder();
                border.setCenter(x, z);
                border.setSize(size);
                border.setDamageBuffer(0);
                border.setDamageAmount(0.5);
                border.setWarningDistance(5);
                border.setWarningTime(15);

                // Wenn Spieler online ist, direkt anwenden
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.setWorldBorder(border);
                }

                // Optional: WorldBorder irgendwo speichern oder zuordnen, z. B. in einer Map<UUID, WorldBorder>
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
        if (size <= 0) size = 50;

        // Individuelle WorldBorder erstellen
        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(x, z);
        border.setSize(size);
        border.setDamageBuffer(0);
        border.setDamageAmount(0.5);
        border.setWarningDistance(5);
        border.setWarningTime(15);

        // Spieler bekommt seine eigene WorldBorder
        player.setWorldBorder(border);

        // OneBlock-Log an der richtigen Stelle setzen
        World playerWorld = player.getWorld();
        Location blockLocation = new Location(playerWorld, x, 100, z);
        Block block = blockLocation.getBlock();

        if (block.getType() == Material.AIR) {
            block.setType(Material.OAK_LOG);
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