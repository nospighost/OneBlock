package de.Main.OneBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Generator implements Listener {

    private final JavaPlugin plugin;

    public Generator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        Location generator = new Location(player.getWorld(), 9995, 102, 9995);

        if (loc.equals(generator)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    generator.getBlock().setType(Material.STONE);
                    event.getBlock().getWorld().dropItemNaturally(loc, new ItemStack(Material.STONE));
                }
            }.runTaskLater(plugin, 1L);
        }
    }
}