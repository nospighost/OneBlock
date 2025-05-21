package de.Main.OneBlock;

import org.bukkit.*;

import org.bukkit.block.Block;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class OBItems implements CommandExecutor, Listener {
    private Inventory globalTrashInventory;
    private final JavaPlugin plugin;
    private Inventory onechest;
    public static File configFile;
    public static YamlConfiguration config;


    public OBItems(JavaPlugin plugin) {
        this.plugin = plugin;
        globalTrashInventory = Bukkit.createInventory(null, 54, "§6Globaler Mülleimer");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können das nutzen!");
            return true;
        }
        Player player = (Player) sender;
        player.openInventory(globalTrashInventory);
        ItemStack magnet = new ItemStack(Material.SADDLE);
        ItemMeta meta2 = magnet.getItemMeta();
        meta2.setLore(config.getStringList("Magnet.lore"));
        magnet.setItemMeta(meta2);
        player.getInventory().addItem(magnet);
        return true;
    }


    public void start() {
        Bukkit.getScheduler().runTaskTimer(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("oneblockplugin")), () -> {
            globalTrashInventory.clear();
        }, 0L, 5 * 60 * 20L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        boolean hasMagnet = isValidMagnet(player.getInventory().getItemInMainHand()) ||
                isValidMagnet(player.getInventory().getItemInOffHand());

        if (!isLog(type)) return;
        if (!isAxe(player.getInventory().getItemInMainHand().getType())) return;

        World world = block.getWorld();
        int startY = block.getY();
        int x = block.getX();
        int z = block.getZ();

        int maxBlocks = 32;

        for (int i = 0; i < maxBlocks; i++) {
            int currentY = startY + i;
            if (currentY > world.getMaxHeight()) break;

            Block current = world.getBlockAt(x, currentY, z);

            if (isLog(current.getType())) {
                Material mat = current.getType();
                Location loc = current.getLocation().add(0.5, 0.5, 0.5);

                current.setType(Material.AIR);


                if (hasMagnet) {
                    player.getInventory().addItem(new ItemStack(mat));
                } else {
                    current.getWorld().dropItemNaturally(loc, new ItemStack(mat));
                }

                world.spawnParticle(Particle.COMPOSTER, loc, 10);

                int delay = i;
                Bukkit.getScheduler().runTaskLater(plugin, () ->
                                world.playSound(loc, Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f)
                        , delay);

            } else {
                break;
            }
        }


        if (hasMagnet) {
            event.setDropItems(false);
        }
    }



    private boolean isLog(Material material) {
        return material.name().endsWith("_LOG") || material.name().endsWith("_STEM");
    }

    private boolean isAxe(Material material) {
        return material.name().endsWith("_AXE");
    }
    private boolean isMagnet(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack mainhand = player.getInventory().getItemInMainHand();
        ItemStack offhand = player.getInventory().getItemInOffHand();


        if (!isValidMagnet(mainhand) && !isValidMagnet(offhand)) return false;


        Block block = event.getBlock();
        Material blockType = block.getType();

        event.setDropItems(false);
        block.setType(Material.AIR);
        player.getInventory().addItem(new ItemStack(blockType));


        return true;
    }

    private boolean isValidMagnet(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;

        List<String> itemLore = meta.getLore();
        List<String> configLore = config.getStringList("Magnet.lore");

        if (itemLore == null || configLore == null) return false;
        if (itemLore.size() < configLore.size()) return false;

        for (int i = 0; i < configLore.size(); i++) {
            if (!itemLore.get(i).equals(configLore.get(i))) return false;
        }

        return true;
    }



    @EventHandler(ignoreCancelled = true)
    public void onShiftRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked.getType() == Material.BEDROCK) {
            player.openInventory(globalTrashInventory);
            createCustomItemsConfig(plugin);

        }

    }

    public static YamlConfiguration getCustomItemsConfig(JavaPlugin plugin) {
        File folder = new File(plugin.getDataFolder(), "CustomItems");
        if (!folder.exists()) {
            folder.mkdirs(); // Ordner erstellenfalls nicht vorhanden
        }

        configFile = new File(folder, "CustomItems.yml");

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        return config;
    }

    public static void createCustomItemsConfig(JavaPlugin plugin) {
        getCustomItemsConfig(plugin);


        config.set("Magnet", "Magnet");
        config.set("Magnet.lore", Collections.singletonList("§7§lEffekt §f§l>> §c§lSammelt Alle Items auf"));
        config.set("Magnet.material", "SADDLE");
        config.set("WoodCutter", "WoodCutter");
        config.set("WoodCutter.lore", Collections.singletonList("§7§lEffekt §f§l>> §c§lBaut den ganzen Baumstamm ab"));)
        config.set("WoodCutter.material", "NETHERITE_AXE");
        ItemStack magnet = config.getItemStack("Magnet.material");
        if (magnet != null && magnet.getType() != Material.AIR) {
            ItemMeta meta = magnet.getItemMeta();
            meta.setLore(Collections.singletonList(config.getString("Magnet.lore")));

        }
        saveCustomItemsConfig();

    }

    public static void saveCustomItemsConfig() {
        try {
            if (config != null && configFile != null) {
                config.save(configFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}










