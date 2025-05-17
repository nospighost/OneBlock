package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.util.UUID;

public class JoinListener implements Listener {

    private final File islandDataFolder;

    public JoinListener(File islandDataFolder) {
        this.islandDataFolder = islandDataFolder;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
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

            player.setWorldBorder(border);
        }
    }
}
