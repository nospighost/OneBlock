package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
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

public class Generator implements Listener {

    private final JavaPlugin plugin;
    private final int multi = 1;

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

        ConfigurationSection blocksSection = config.getConfigurationSection("Blocks");
        if (blocksSection != null) {
            for (String key : blocksSection.getKeys(false)) {
                int x = config.getInt("Blocks." + key + ".x");
                int y = config.getInt("Blocks." + key + ".y");
                int z = config.getInt("Blocks." + key + ".z");

                if (x == loc.getBlockX() && y == loc.getBlockY() && z == loc.getBlockZ()) {
                    String typeString = config.getString("Blocks." + key + ".type");
                    Material material = Material.getMaterial(typeString);

                    if (material != null) {
                        // 1. Richtigen Block droppen
                        ;
                        loc.getWorld().dropItemNaturally(loc, new ItemStack(material, multi));


                        // 2. Generator-Eintrag entfernen

                        try {
                            config.save(file);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                loc.getBlock().setType(material);
                            }
                        }.runTaskLater(plugin, 1L);
                    }
                    break;
                }
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        File file = Manager.getGeneratorFile(loc);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ItemStack item = event.getItemInHand();

        if (!item.containsEnchantment(Enchantment.SILK_TOUCH)) return;

        int enchantLevel = item.getEnchantmentLevel(Enchantment.SILK_TOUCH);
        if (enchantLevel < 5) return;


        String blockType = event.getBlock().getType().toString();

        String key = blockType + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();

        config.set("Blocks." + key + ".type", blockType);
        config.set("Blocks." + key + ".x", loc.getBlockX());
        config.set("Blocks." + key + ".y", loc.getBlockY());
        config.set("Blocks." + key + ".z", loc.getBlockZ());
        try {
            config.save(file);
            player.sendMessage("Generator gesetzt bei " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

