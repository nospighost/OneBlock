package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static de.Main.OneBlock.Manager.*;

public class Generator implements Listener {

    private final JavaPlugin plugin;
    private int multi = 1;

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


    @EventHandler
    public void onShiftRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
            Block block = event.getClickedBlock();

            if (block != null && block.getType() == Material.STONE) {
                Inventory gui = Bukkit.createInventory(null, 27, "§aUpgrade-GUI");

                ItemStack item = new ItemStack(Material.STONE);
                ItemMeta meta = item.getItemMeta();

                meta.addEnchant(Enchantment.SILK_TOUCH, 5, true);

                gui.setItem(4, item);

                player.openInventory(gui);

                event.setCancelled(true);
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();
        YamlConfiguration config = getIslandConfig(player.getUniqueId());


        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();
        Material type = clicked.getType();

        if (title.equalsIgnoreCase("§aUpgrade-GUI")) {
            event.setCancelled(true);

            switch (type) {

                case STONE: {

                    int currentUpgrade = 1;

                    int basePrice = 20000;
                    int maxUpgrades = 20;
                    int upgradesDone = (currentUpgrade - 50) / 10;

                    if (upgradesDone >= maxUpgrades) {
                        player.sendMessage("§cDu hast das maximale Upgrade-Level erreicht.");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        return;
                    }

                    int price = basePrice * 2;
                    if (economy.getBalance(player) < price) {
                        player.sendMessage("§cDu hast nicht genug Geld! Benötigt: §e" + price + " Coins");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        return;
                    }

                    economy.withdrawPlayer(player, price);

                    multi ++;


                    player.sendMessage("§aUpgrade erfolgreich! Neuer Preis für nächstes Upgrade: §e" +
                            (int) (basePrice * Math.pow(2, upgradesDone + 1)) + " Coins");

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

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

        if (item.containsEnchantment(Enchantment.FORTUNE));

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