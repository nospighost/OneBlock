package de.Main.OneBlock.NPC.Listener;

import de.Main.OneBlock.NPC.GUI.NPCGUI;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;


import static de.Main.OneBlock.NPC.Manager.NPCManager.openTradeMenu;

public class NPCListener implements Listener {
public static String NPCName = "§a§lHändler";
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(!(event.getRightClicked() instanceof Villager))return;
        Villager villager = (Villager) event.getRightClicked();
        if (!NPCName.equals(villager.getCustomName())) return;
        event.setCancelled(true); // Standard Interaktion verhindern
        openTradeMenu(event.getPlayer(), NPCGUI.mainGUI);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity().getType() == EntityType.VILLAGER && event.getEntity().getCustomName().equals(NPCName)){
            event.setCancelled(true);
        }
    }

}
