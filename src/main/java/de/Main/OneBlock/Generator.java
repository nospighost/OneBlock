package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Generator implements Listener {

    private final JavaPlugin plugin;

    public Generator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        File file = Manager.getGeneratorFile(loc);
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String worldName = config.getString("generator.world");
        int x = config.getInt("generator.x");
        int y = config.getInt("generator.y");
        int z = config.getInt("generator.z");

        Location generator = new Location(Bukkit.getWorld(worldName), x, y, z);

        if (!loc.equals(generator)) return;

        // Drop manuell erzeugen
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.STONE));

        // Regeneriere Block nach 1 Tick
        new BukkitRunnable() {
            @Override
            public void run() {
                loc.getBlock().setType(Material.STONE);
            }
        }.runTaskLater(plugin, 1L);
    }


    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        File file = Manager.getGeneratorFile(loc);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("generator.world", loc.getWorld().getName());
        config.set("generator.x", loc.getBlockX());
        config.set("generator.y", loc.getBlockY());
        config.set("generator.z", loc.getBlockZ());

        try {
            config.save(file);
            player.sendMessage("Generator gesetzt bei " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


