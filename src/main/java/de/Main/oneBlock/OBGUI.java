package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OBGUI implements CommandExecutor {

    // Speichert das GUI, damit es nicht mehrfach erstellt wird
    public static Inventory mainGUI;
    public static Inventory OBLÖSCHUNG;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;

        if (mainGUI == null) {
            mainGUI = Bukkit.createInventory(null, 6 * 9, "OneBlock");
        }

        // Itemstack erstellen...
        player.openInventory(mainGUI);
        return true;
    }

    public static void openmaingui(Player player) {
        if (OBLÖSCHUNG == null) {
            OBLÖSCHUNG = Bukkit.createInventory(null, 3 * 9, "OneBlock-Löschung");
        }


        ItemStack itemstack = new ItemStack(Material.STONE);
        ItemMeta meta = itemstack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Test");
            itemstack.setItemMeta(meta);
        }

        OBLÖSCHUNG.setItem(14, itemstack);

        player.openInventory(OBLÖSCHUNG);
    }
}
