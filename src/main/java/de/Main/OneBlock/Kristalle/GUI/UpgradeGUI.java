package de.Main.OneBlock.Kristalle.GUI;

import de.Main.OneBlock.Kristalle.Listener.GrowthManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UpgradeGUI {

    private final GrowthManager growthManager;
    private final Economy economy;

    public UpgradeGUI(GrowthManager growthManager, Economy economy) {
        this.growthManager = growthManager;
        this.economy = economy;
    }

    public void open(Player player, Location blockLocation) {
        Inventory gui = Bukkit.createInventory(null, 9, "§bUpgrade-Menü");

        int level = growthManager.getLevel(blockLocation);

        // Upgrade Item
        ItemStack upgrade = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta upgradeMeta = upgrade.getItemMeta();
        if (upgradeMeta != null) {
            upgradeMeta.setDisplayName("§aKristall-Upgrade");
            List<String> lore = new ArrayList<>();
            lore.add("§eAktuelles Level: §b" + level);
            lore.add("§eNach dem Upgrade: §b" + (level + 1));
            lore.add("§eKosten: §6" + getUpgradePrice(level) + "$");
            lore.add("§aVerdienst danach: §e" + (level + 2) + "$");
            upgradeMeta.setLore(lore);
            upgrade.setItemMeta(upgradeMeta);
        }

        // Abbau Item (Barrier)
        ItemStack remove = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = remove.getItemMeta();
        if (removeMeta != null) {
            removeMeta.setDisplayName("§cKristall abbauen");
            List<String> lore = new ArrayList<>();
            lore.add("§7Klicke hier, um den Kristall abzubauen.");
            lore.add("§7Benötigt Treue (Loyalty) Stufe 5+ auf deinem Werkzeug.");
            lore.add("§eDein aktuelles Level wird gespeichert.");
            removeMeta.setLore(lore);
            remove.setItemMeta(removeMeta);
        }

        gui.setItem(3, upgrade);
        gui.setItem(5, remove);

        player.openInventory(gui);
    }

    public int getUpgradePrice(int level) {
        return (int) (1000 * Math.pow(2, level + 1)); // Preis wächst exponentiell
    }
}
