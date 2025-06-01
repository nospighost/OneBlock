package de.Main.OneBlock.NPC.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NPCGUI {
        public static String npcMainGUIName = "Händler";
    public static String npcGUIBookName = "Aufgaben";
    public static String npcGUIExperienceBottleName = "Verkaufen";
    public static Inventory mainGUI = Bukkit.createInventory(null, 27, npcMainGUIName);
    public static Inventory bookGUI = Bukkit.createInventory(null, 27, npcGUIBookName);
    public static Inventory experienceGUI = Bukkit.createInventory(null, 27, npcGUIExperienceBottleName);

    public static void createNPCGUI() {
        ItemStack xp = new ItemStack(Material.BOOK);
        ItemMeta xpMeta = xp.getItemMeta();
        xpMeta.setDisplayName("§cAufgaben");
        xp.setItemMeta(xpMeta);
        mainGUI.setItem(12, xp);
        ItemStack book = new ItemStack(Material.CHEST);
        ItemMeta bookMeta = xp.getItemMeta();
        bookMeta.setDisplayName("§cItem Verkaufen");
        book.setItemMeta(bookMeta);
        mainGUI.setItem(14, book);
    }
}
