package de.Main.OneBlock.Quest.Listener;

import de.Main.OneBlock.database.DBM;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class QuestListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = event.getPlayer().getUniqueId();
        Material block = event.getBlock().getType();


       Integer breaked100stone = DBM.getInt("quest", uuid, "broken100stone", 0);

       if (block == Material.STONE){
           breaked100stone++;

           DBM.setInt("quest", uuid, "broken100stone", breaked100stone);
       }
       if (breaked100stone == 100){
           player.sendMessage("Broken 100 stone");
       }


    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = event.getPlayer().getUniqueId();
        Material block = event.getBlock().getType();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
    }



}
