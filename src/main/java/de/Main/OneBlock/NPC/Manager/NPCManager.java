package de.Main.OneBlock.NPC.Manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class NPCManager {
    public static void createNPC(Location location) {
        Villager NPC = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        NPC.setVillagerType(Villager.Type.JUNGLE);
        NPC.setCustomName("§a§lHändler");
        NPC.setAI(false); // KI deaktivieren && Schwerkraft && moven
        NPC.setSilent(true); //Sounds deaktivieren
        NPC.setCanPickupItems(false);
        NPC.setVillagerLevel(1);
        NPC.setCustomNameVisible(true);
    }
    public static void openTradeMenu(Player player, Inventory tradeMenu) {


        if(tradeMenu == null){
            tradeMenu.setItem(0, new ItemStack(Material.BOOK, 1));
            tradeMenu.setItem(1, new ItemStack(Material.EXPERIENCE_BOTTLE, 5));
        }


        player.openInventory(tradeMenu);
    }
}
