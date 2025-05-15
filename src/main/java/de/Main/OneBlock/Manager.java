package de.Main.OneBlock;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.Main.OneBlock.Main.*;

public class Manager implements Listener {
    public static Economy economy; // <-- jetzt static

    public Manager(Economy eco) {
        economy = eco;
    }

    public static boolean createOrJoinIsland(Player player, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("join")) {

            YamlConfiguration config = Manager.getIslandConfig(player);
            if (!config.contains("IslandSpawn-x") || !config.getBoolean("EigeneInsel") || !config.contains("IslandSpawn-z") || !config.contains("OneBlock-x") || !config.contains("OneBlock-z")) {

                Integer z = getIslandCords(config.getInt("value"));
                oneBlockWorld.setSpawnLocation(config.getInt("IslandSpawn-x"), 100, config.getInt("IslandSpawn-z"));
                config.set("OneBlock-x", z);
                config.set("OneBlock-z", z);
                config.set("z-position", z);
                config.set("x-position", z);
                config.set("IslandSpawn-x", z); //InselPosition
                config.set("IslandSpawn-z", z); //InselPosition
                config.set("WorldBorderSize", 50);
                config.set("EigeneInsel", true);


                Manager.saveIslandConfig(player, config);
                Main.setWorldBorder(player); // Welt Border wird erstellt mit dem Player
                World world = Bukkit.getWorld("OneBlock");
                if (world != null) {
                    Location spawn = new Location(world, config.getInt("IslandSpawn-x"), 101, config.getInt("IslandSpawn-z"));
                    player.teleport(spawn);

                }
                player.sendMessage(Main.config.getString("islandjoinmessage.notowned"));
            } else {
                World world = Bukkit.getWorld("OneBlock");
                if (world != null) {
                    Location spawn = new Location(world, config.getInt("IslandSpawn-x"), 101, config.getInt("IslandSpawn-z"));
                    player.teleport(spawn);
                    Main.setWorldBorder(player);
                    player.sendMessage(Main.config.getString("islandjoinmessage.join"));
                } else {
                    player.sendMessage("§cOneBlock-Welt nicht gefunden!");
                }
            }
            return true;
        }
        player.sendMessage("§cNutze: /ob join");
        return true;
    }

    public static File getIslandFile(Player player) {
        return new File(Main.islandDataFolder, player.getName() + ".yml");
    }

    public static YamlConfiguration getIslandConfig(Player player) {
        File file = getIslandFile(player);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void saveIslandConfig(Player player, YamlConfiguration config) {
        try {
            config.save(getIslandFile(player));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getIslandName(Player player) {
        YamlConfiguration config = getIslandConfig(player);
        return config.getString("name", "UnbenannteInsel");
    }

    public static void setIslandName(Player player, String name) {
        YamlConfiguration config = getIslandConfig(player);
        config.set("name", name);
        saveIslandConfig(player, config);
    }

    public static List<String> getAllIslandOwners() {
        List<String> owners = new ArrayList<>();
        File folder = Main.islandDataFolder;
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    String ownerName = fileName.substring(0, fileName.length() - 4);
                    owners.add(ownerName);
                }
            }
        }
        return owners;
    }


    public static int getIslandCords(Integer x) {
        x = config.getInt("InselPadding.value") + 400;
        System.out.println("Voher:" + x);
        config.set("InselPadding.value", x);
        Main.getInstance().saveConfig();
        return x;
    }

    public static void deleteIsland(Player player) {
        YamlConfiguration config = getIslandConfig(player);
        World world = Bukkit.getWorld("OneBlock");

        if (world == null) {
            player.sendMessage("§4OneBlock-Welt nicht gefunden.");
            return;
        }

        if (!config.getBoolean("EigeneInsel", false)) {
            player.sendMessage("§aDu besitzt keine Insel.");
            return;
        }

        int x = config.getInt("OneBlock-x");
        int z = config.getInt("OneBlock-z");
        int size = config.getInt("WorldBorderSize", 50);


        player.teleport(new Location(world, 0, 100, 0));


        for (int dx = -size / 2; dx <= size / 2; dx++) {
            for (int dz = -size / 2; dz <= size / 2; dz++) {
                for (int dy = 90; dy <= 110; dy++) {
                    Location loc = new Location(world, x + dx, dy, z + dz);
                    world.getBlockAt(loc).setType(Material.AIR);
                }
            }
        }


        config.set("EigeneInsel", false);
        config.set("IslandLevel", 1);
        config.set("MissingBlocksToLevelUp", 200);
        config.set("TotalBlocks", 200);
        config.set("IslandSpawn-x", null);
        config.set("IslandSpawn-z", null);
        config.set("x-position", null);
        config.set("z-position", null);
        config.set("OneBlock-x", null);
        config.set("OneBlock-z", null);
        config.set("WorldBorderSize", 50);
//
        saveIslandConfig(player, config);

        player.sendMessage("§aDeine Insel wurde vollständig gelöscht.");
    }


    public static void visitIsland(Player visitor, String ownerName) {
        // Dateipfad zur Insel-Konfig
        File file = new File("plugins/OneBlockPlugin/IslandData", ownerName + ".yml");

        if (!file.exists()) {
            visitor.sendMessage("§cDie Insel von §e" + ownerName + " §cwurde nicht gefunden.");
            return;
        }

        // Konfiguration laden
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        World world = Bukkit.getWorld("OneBlock");

        if (world == null) {
            visitor.sendMessage("§cDie Welt 'OneBlock' konnte nicht gefunden werden.");
            return;
        }

        // Koordinaten auslesen
        int x = config.getInt("IslandSpawn-x");
        int z = config.getInt("IslandSpawn-z");

        // Teleportieren
        Location spawn = new Location(world, x, 101, z);
        visitor.teleport(spawn);
        visitor.sendMessage("§aDu wurdest zur Insel von §e" + ownerName + " §ateleportiert.");
    }


    public static void rebirthIsland(Player player) {
        YamlConfiguration config = getIslandConfig(player);

        config.set("IslandLevel", 1);
        config.set("TotalBlocks", 200);
        config.set("MissingBlocksToLevelUp", 200);


        ItemStack stack = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName("§cRebirth Pickaxe");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(" ");
        lore.add(ChatColor.WHITE + "Diese Spitzhacke bekommst du beim Rebirth");
        lore.add("");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.EFFICIENCY, 6, true);

        meta.addEnchant(Enchantment.UNBREAKING, 5, true);

        meta.addEnchant(Enchantment.MENDING, 1, true);

        meta.addEnchant(Enchantment.FORTUNE, 2, true);
        stack.setItemMeta(meta);

        player.getInventory().addItem(stack); //a

        economy.depositPlayer(player, 1000);

        player.sendMessage("§aDeine Insel wurde erfolgreich Rebirthed");
        saveIslandConfig(player, config);
    }

}