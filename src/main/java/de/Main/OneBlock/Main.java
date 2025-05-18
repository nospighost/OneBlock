package de.Main.OneBlock;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main extends JavaPlugin implements Listener {

    private static Main instance;
    private static final String WORLD_NAME = "OneBlock";
    public static World oneBlockWorld;
    public static FileConfiguration config;
    public static File islandDataFolder;
    private static Economy economy = null;

    // AdvancedChest-Daten
    private final Map<Location, ChestData> chests = new HashMap<>();
    private File dataFile;
    private YamlConfiguration dataConfig;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        config = getConfig();
        setupEconomy();

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        if (economy != null) {
            Bukkit.getPluginManager().registerEvents(new Manager(economy, this), this);
            getLogger().info("Vault Economy erfolgreich erkannt.");
        } else {
            getLogger().warning("Vault wurde nicht gefunden – Economy wird deaktiviert.");
        }

        getCommand("ob").setExecutor(new OneBlockCommands());
        getCommand("obgui").setExecutor(new OBGUI());
        getCommand("ob").setTabCompleter(new TabCompleter());
        getServer().getPluginManager().registerEvents(new OBGUI(), this);

        // Insel-Datenordner
        islandDataFolder = new File(getDataFolder(), "IslandData");
        if (!islandDataFolder.exists()) islandDataFolder.mkdirs();

        // OneBlock-Welt erzeugen
        WorldCreator worldCreator = new WorldCreator(WORLD_NAME);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.type(WorldType.FLAT);
        worldCreator.generator(new VoidGen());

        oneBlockWorld = Bukkit.createWorld(worldCreator);

        if (oneBlockWorld != null) {
            getLogger().info("OneBlock-Welt wurde erfolgreich erstellt!");
            oneBlockWorld.setSpawnLocation(0, 100, 0);
            WorldBorder worldBorder = oneBlockWorld.getWorldBorder();
            worldBorder.setCenter(0, 0);
            worldBorder.setSize(50);
            worldBorder.setDamageBuffer(0);
            worldBorder.setDamageAmount(0.5);
            worldBorder.setWarningDistance(5);
            worldBorder.setWarningTime(15);
        }

        for (UUID playerUUID : Manager.getAllIslandOwners()) {
            File file = new File(islandDataFolder, playerUUID + ".yml");
            if (file.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                int x = config.getInt("OneBlock-x");
                int z = config.getInt("OneBlock-z");
                int size = config.getInt("WorldBorderSize", 50);
                Player player = Bukkit.getPlayer(playerUUID);
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

        // AdvancedChest-Logik laden
        loadChestData();
        startAutoSell();
    }

    @Override
    public void onDisable() {
        Manager.saveIslandConfig(null, null);
        saveDefaultConfig();
        saveChestData();
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
        if (rsp != null) economy = rsp.getProvider();
        return economy != null;
    }

    public static Economy getEconomy() {
        return economy;
    }

    // ----------------------- AdvancedChest Code -----------------------

    private void loadChestData() {
        dataFile = new File(getDataFolder(), "chests.yml");
        if (!dataFile.exists()) saveResource("chests.yml", false);
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : dataConfig.getKeys(false)) {
            Location loc = Location.deserialize(dataConfig.getConfigurationSection(key).getValues(false));
            int level = dataConfig.getInt(key + ".level");
            chests.put(loc, new ChestData(level));
        }
    }

    private void saveChestData() {
        for (Map.Entry<Location, ChestData> entry : chests.entrySet()) {
            dataConfig.set(entry.getKey().hashCode() + "", entry.getKey().serialize());
            dataConfig.set(entry.getKey().hashCode() + ".level", entry.getValue().level);
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startAutoSell() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, ChestData> entry : chests.entrySet()) {
                    ChestData chest = entry.getValue();
                    int earned = 0;
                    Iterator<ItemStack> it = chest.contents.iterator();
                    while (it.hasNext()) {
                        ItemStack item = it.next();
                        if (item != null) {
                            earned += item.getAmount() * 5;
                            it.remove();
                        }
                    }
                    if (earned > 0) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getLocation().distance(entry.getKey()) < 10)
                                p.sendMessage("§aChest hat " + earned + "$ verkauft!");
                        }
                    }
                }
            }
        }.runTaskTimer(this, 200L, 600L);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.hasItem() && e.getItem().getType() == Material.CHEST) {
            Block block = e.getClickedBlock();
            if (block == null) return;
            Location loc = block.getLocation().add(0, 1, 0);
            loc.getBlock().setType(Material.ENDER_CHEST);
            chests.put(loc, new ChestData(0));
            e.getPlayer().sendMessage("§aAdvancedChest platziert!");
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().startsWith("§6Chest Lvl ")) {
            Player p = (Player) e.getWhoClicked();
            Location loc = findChest(p.getLocation());
            if (loc == null) return;
            ChestData data = chests.get(loc);
            Bukkit.getScheduler().runTaskLater(this, () -> {
                data.contents = Arrays.asList(e.getInventory().getContents());
            }, 1L);
        }
    }

    private Location findChest(Location near) {
        for (Location loc : chests.keySet()) {
            if (loc.getWorld().equals(near.getWorld()) && loc.distance(near) < 5)
                return loc;
        }
        return null;
    }

    static class ChestData {
        int level;
        List<ItemStack> contents = new ArrayList<>();
        ChestData(int level) { this.level = level; }
    }
}
