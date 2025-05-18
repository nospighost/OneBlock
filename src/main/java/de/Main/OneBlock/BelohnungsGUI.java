package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static de.Main.OneBlock.Manager.getIslandConfig;
import static org.bukkit.Material.EXPERIENCE_BOTTLE;

public class BelohnungsGUI implements CommandExecutor, Listener {

    public static Inventory BelohnungsGUI;


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        if (BelohnungsGUI == null) {
            createguis(player);
        }

        return true;
    }


    private void createguis(Player player) {
        YamlConfiguration config = getIslandConfig(player.getUniqueId());

        BelohnungsGUI = Bukkit.createInventory(null, 9 * 5, "§cOneBlock-Belohnungen");

        ItemStack xpBottle = new ItemStack(EXPERIENCE_BOTTLE);
        ItemMeta meta6 = xpBottle.getItemMeta();
        if (meta6 != null) {
            meta6.setDisplayName("§aPhasen-Auswahl");
            List<String> lore = new ArrayList<>();

            lore.add(" ");
            lore.add("§fKlicke um in das Phasen-Auswahl-Menü zu gelangen!");
            lore.add(" ");


            meta6.setLore(lore);
            xpBottle.setItemMeta(meta6);
        }
        BelohnungsGUI.setItem(3, xpBottle);

    }
}
