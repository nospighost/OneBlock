package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;



public class WorldBorderManager implements Listener {
@EventHandler
public void onPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    YamlConfiguration config = Manager.getIslandConfig(player.getUniqueId());

    int centerX = config.getInt("x-position");
    int centerZ = config.getInt("z-position");
    int borderSize = config.getInt("WorldBorderSize");

    double dx = event.getTo().getX() - centerX;
    double dz = event.getTo().getZ() - centerZ;

    double distanceSquared = dx * dx + dz * dz;
    double maxDistance = borderSize / 2.0;

    if (distanceSquared > maxDistance * maxDistance) {
        player.sendMessage("§cDu kannst deine Insel nicht verlassen!");
        event.setCancelled(true);
    }

    WorldBorder border = Bukkit.createWorldBorder();
    border.setCenter(centerZ, centerX); // Inselmittelpunkt
    border.setSize(config.getInt("WorldBorderSize" ) * 2 ); // Durchmesser!

}
}
