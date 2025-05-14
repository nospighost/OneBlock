package de.Main.OneBlock;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OBGUI implements CommandExecutor, Listener {

    public static Inventory mainGUI;
    public static Inventory UpgradeShop;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;


        if (UpgradeShop == null) {
            UpgradeShop = Bukkit.createInventory(null, 6 * 9, "Upgrade-Shop");

            ItemStack shop = new ItemStack(Material.NETHER_STAR);
            ItemMeta shopMeta = shop.getItemMeta();
            if (shopMeta != null) {
                shopMeta.setDisplayName("§aWorldBorder vergrößern!");
                shop.setItemMeta(shopMeta);
            }
            UpgradeShop.setItem(20, shop);
        }

        player.openInventory(UpgradeShop);





        if (mainGUI == null) {
            mainGUI = Bukkit.createInventory(null, 6 * 9, "OneBlock");

            // Schwarze Glasscheiben für den Rand
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

            // Item: Zu deinem OneBlock teleportieren
            ItemStack grass = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta grassMeta = grass.getItemMeta();
            if (grassMeta != null) {
                grassMeta.setDisplayName("§aZu deinem OneBlock teleportieren");
                grass.setItemMeta(grassMeta);
            }
            mainGUI.setItem(20, grass);

            // Item: OneBlock löschen
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta barrierMeta = barrier.getItemMeta();
            if (barrierMeta != null) {
                barrierMeta.setDisplayName("§cDeinen OneBlock löschen");
                barrier.setItemMeta(barrierMeta);
            }
            mainGUI.setItem(22, barrier);

            // Item: Andere Insel besuchen
            ItemStack visit = new ItemStack(Material.ENDER_PEARL);
            ItemMeta visitMeta = visit.getItemMeta();
            if (visitMeta != null) {
                visitMeta.setDisplayName("§bAndere Insel besuchen");
                visit.setItemMeta(visitMeta);
            }
            mainGUI.setItem(24, visit);

            ItemStack shop = new ItemStack(Material.BEACON);
            ItemMeta shopMeta = shop.getItemMeta();
            if (shopMeta != null) {
                shopMeta.setDisplayName("§aZum OneBlock Shop");
                shop.setItemMeta(shopMeta);
            }
            mainGUI.setItem(28, shop);

        }

        player.openInventory(mainGUI);
        return true;
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        if (!event.getView().getTitle().equalsIgnoreCase("OneBlock")) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        Material type = clicked.getType();

        switch (type) {
            case GRASS_BLOCK:
                player.closeInventory();
                player.performCommand("ob join");
                break;

            case BARRIER:
                player.closeInventory();
                player.performCommand("ob delete");
                break;

            case ENDER_PEARL:
                player.closeInventory();
                break;

            case BEACON:
                player.closeInventory();
                player.openInventory(UpgradeShop);



                // Soundeffekt beim Klick (optional)
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

                // Nachricht mit klickbarem Vorschlag
                TextComponent msg = new TextComponent("§aKlicke hier, um den Besuchsbefehl einzugeben: ");
                TextComponent commandPart = new TextComponent("§e/ob visit ");

                commandPart.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ob visit "));
                msg.addExtra(commandPart);

                player.spigot().sendMessage(msg);
                break;
            default:
                break;
        }
    }
}
