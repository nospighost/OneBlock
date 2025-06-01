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

    private static final int MAX_LEVEL = 20;
    private static final int MAX_PRESTIGE = 10;

    public UpgradeGUI(GrowthManager growthManager, Economy economy) {
        this.growthManager = growthManager;
        this.economy = economy;
    }

    public void open(Player player, Location blockLocation) {
        Inventory gui = Bukkit.createInventory(null, 9, "§bUpgrade-Menü");

        int level = growthManager.getLevel(blockLocation);
        int prestige = growthManager.getPrestige(blockLocation);

        // Upgrade Item
        ItemStack upgrade = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta upgradeMeta = upgrade.getItemMeta();
        if (upgradeMeta != null) {
            upgradeMeta.setDisplayName("§aKristall-Upgrade");
            List<String> lore = new ArrayList<>();
            lore.add("§eAktuelles Level: §b" + level);
            lore.add("§eNach dem Upgrade: §b" + Math.min(level + 1, MAX_LEVEL));
            lore.add("§eKosten: §6" + getUpgradePrice(level) + "$");
            lore.add("§aVerdienst danach: §e" + (level + 2) + "$");
            if (level >= MAX_LEVEL) lore.add("§cMaximales Level erreicht!");
            upgradeMeta.setLore(lore);
            upgrade.setItemMeta(upgradeMeta);
            if(level >= MAX_LEVEL) upgrade.setAmount(0); // Deaktiviert optisch
        }

        // Prestige Item
        ItemStack prestigeItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta prestigeMeta = prestigeItem.getItemMeta();
        if (prestigeMeta != null) {
            prestigeMeta.setDisplayName("§aPrestige");
            List<String> lore = new ArrayList<>();
            lore.add("§bSetzt Kristall-Level zurück!");
            lore.add("§bMaximaler Prestige: §e" + MAX_PRESTIGE);
            lore.add("§eAktueller Prestige: §b" + prestige);
            if(prestige >= MAX_PRESTIGE) lore.add("§cMaximaler Prestige erreicht!");
            else lore.add("§aKlicke zum Prestigen!");
            prestigeMeta.setLore(lore);
            prestigeItem.setItemMeta(prestigeMeta);
            if(prestige >= MAX_PRESTIGE) prestigeItem.setAmount(0);
        }

        gui.setItem(3, upgrade);
        gui.setItem(5, prestigeItem);

        player.openInventory(gui);
    }

    public int getUpgradePrice(int level) {
        return (int) (1000 * Math.pow(2, level + 1));
    }
}
