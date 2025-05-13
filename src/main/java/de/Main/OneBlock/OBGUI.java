package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OBGUI implements CommandExecutor, Listener {

    // Speichert das GUI, damit es nicht mehrfach erstellt wird
    public static Inventory mainGUI;
    public static Inventory OBLÖSCHUNG;
int[] GLASS1 = {10, 20, 30, 40};
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;

        if (mainGUI == null) {
            mainGUI = Bukkit.createInventory(null, 6 * 9, "OneBlock");

            ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta glassMeta = blackGlass.getItemMeta();
            if (glassMeta != null) {
                glassMeta.setDisplayName(" ");
                blackGlass.setItemMeta(glassMeta);
            }

            for (int i = 0; i < 54; i++) {
                if (i <= 8 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                    mainGUI.setItem(i, blackGlass);
                }
            }

            ItemStack grass = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = grass.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§aZu deinem OneBlock Teleportieren");
                grass.setItemMeta(meta);
            }

            mainGUI.setItem(20, grass);
        }

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cDeinen OneBlock löschen");
            barrier.setItemMeta(meta);
        }

        mainGUI.setItem(22, barrier);






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

    //nospi der Command

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() == null) return;
        if (event.getCurrentItem() == null) return;

        ItemStack clickedItem = event.getCurrentItem();


        if (event.getView().getTitle().equals("OneBlock")) {
            event.setCancelled(true);

            Material type = clickedItem.getType();

            if (type == Material.GRASS_BLOCK) {
                player.closeInventory();
                player.performCommand("ob join");
            } else if (type == Material.BARRIER) {
                player.closeInventory();
                player.performCommand("ob delete");
            }
        }


    }



}
