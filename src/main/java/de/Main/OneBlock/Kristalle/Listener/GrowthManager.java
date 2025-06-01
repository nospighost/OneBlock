package de.Main.OneBlock.Kristalle.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class GrowthManager {

    static FileConfiguration growthConfig;
    private static File growthFile;

    private static final int MAX_LEVEL = 20;
    private static final int MAX_PRESTIGE = 10;

    public GrowthManager(FileConfiguration growthConfig, File growthFile) {
        this.growthConfig = growthConfig;
        this.growthFile = growthFile;
    }

    public static String getPath(Location loc) {
        return "growth." + loc.getWorld().getName() + "." + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    public static int getLevel(Location loc) {
        return growthConfig.getInt(getPath(loc) + ".Level", 0);
    }

    public static int getPrestige(Location loc) {
        return growthConfig.getInt(getPath(loc) + ".Prestige", 0);
    }

    public static void setLevel(Location loc, int level) {
        if (level > MAX_LEVEL) level = MAX_LEVEL;
        growthConfig.set(getPath(loc) + ".Level", level);
        saveConfig();
    }

    public static void setPrestige(Location loc, int prestige) {
        if (prestige > MAX_PRESTIGE) prestige = MAX_PRESTIGE;
        growthConfig.set(getPath(loc) + ".Prestige", prestige);
        saveConfig();
    }

    public static void saveGrowth(Location loc, Material stage, long nextGrowthMillis, UUID owner, int level, int prestige) {
        String path = getPath(loc);
        growthConfig.set(path + ".stage", stage.name());
        growthConfig.set(path + ".nextGrowth", nextGrowthMillis);
        growthConfig.set(path + ".owner", owner != null ? owner.toString() : null);
        growthConfig.set(path + ".isFullyGrown", stage == Material.AMETHYST_CLUSTER);
        setLevel(loc, level);
        setPrestige(loc, prestige);
    }

    public static Material getNextStage(Material stage) {
        switch (stage) {
            case SMALL_AMETHYST_BUD:
                return Material.MEDIUM_AMETHYST_BUD;
            case MEDIUM_AMETHYST_BUD:
                return Material.LARGE_AMETHYST_BUD;
            case LARGE_AMETHYST_BUD:
                return Material.AMETHYST_CLUSTER;
            default:
                return null;
        }
    }

    public static int getGrowthTimeSeconds(Material stage) {
        switch (stage) {
            case SMALL_AMETHYST_BUD: return 3;
            case MEDIUM_AMETHYST_BUD: return 5;
            case LARGE_AMETHYST_BUD: return 7;
            default: return 0;
        }
    }

    public static void saveConfig() {
        try {
            growthConfig.save(growthFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startGrowthTasks(JavaPlugin plugin, FileConfiguration growthConfig) {
        if (!GrowthManager.growthConfig.isConfigurationSection("growth")) return;

        for (String world : GrowthManager.growthConfig.getConfigurationSection("growth").getKeys(false)) {
            for (String coordKey : GrowthManager.growthConfig.getConfigurationSection("growth." + world).getKeys(false)) {
                String path = "growth." + world + "." + coordKey;
                String stageName = GrowthManager.growthConfig.getString(path + ".stage");
                Material stage = (stageName != null) ? Material.getMaterial(stageName) : null;
                long nextGrowth = GrowthManager.growthConfig.getLong(path + ".nextGrowth", 0);
                Location loc = parseLocation(world, coordKey);
                if (loc == null || stage == null) continue;

                long now = System.currentTimeMillis();

                if (nextGrowth > now && stage != Material.AMETHYST_CLUSTER) {
                    long delayTicks = (nextGrowth - now) / 50;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Block block = loc.getBlock();
                            if (block.getType() == stage) {
                                Material nextStage = getNextStage(stage);
                                if (nextStage != null) {
                                    block.setType(nextStage);
                                    long newNextGrowth = (nextStage == Material.AMETHYST_CLUSTER) ? 0 : System.currentTimeMillis() + getGrowthTimeSeconds(nextStage) * 1000L;
                                    saveGrowth(loc, nextStage, newNextGrowth, UUID.fromString(GrowthManager.growthConfig.getString(path + ".owner")), getLevel(loc), getPrestige(loc));
                                }
                            }
                        }
                    }.runTaskLater(plugin, delayTicks);
                }
            }
        }
    }

    private static Location parseLocation(String worldName, String coordKey) {
        String[] coords = coordKey.split("_");
        if (coords.length != 3) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        try {
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
