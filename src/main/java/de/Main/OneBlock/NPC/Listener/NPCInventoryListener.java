package de.Main.OneBlock.NPC.Listener;

import de.Main.OneBlock.NPC.GUI.NPCGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class NPCInventoryListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();

        // Überprüfen, ob ein Item angeklickt wurde
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        Material type = clicked.getType();
        if (title.equals(NPCGUI.npcMainGUIName)) {
            event.setCancelled(true);
            switch (type) {
                case BOOK -> player.openInventory(NPCGUI.bookGUI);
                case CHEST -> player.openInventory(NPCGUI.experienceGUI);
            }
        }
    }
}
