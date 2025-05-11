package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Manager implements Listener {

    public static boolean createOrJoinIsland(Player player, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("join")) {
            YamlConfiguration config = Manager.getIslandConfig(player);
            if (!config.contains("IslandSpawn-x") || !config.contains("IslandSpawn-z")) {
                config.set("OneBlock-x", 400);
                config.set("OneBlock-z", 400);
                config.set("IslandSpawn-x", 400); //InselPosition
                config.set("IslandSpawn-z", 400); //InselPosition
                config.set("EigeneInsel", true);
                config.set("WorldBorderSize", 50);

                Manager.saveIslandConfig(player, config);
                Main.setWorldBorder(player); // Welt Border wird erstellt mit dem Player
                World world = Bukkit.getWorld("OneBlock");
                if (world != null) {
                    Location spawn = new Location(world, 400, 101, 400);
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



public static int getIslandCords(Player player){

   return 1;
}



}
