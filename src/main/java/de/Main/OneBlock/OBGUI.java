package de.Main.OneBlock;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Material.*;

public class OBGUI implements CommandExecutor, Listener {

    int[] grayglasmaingui = {0, 1, 7, 8};

    public static Inventory upgradeShop;
    public static Inventory mainGUI;
    public static Inventory Einstellungen;
    public static Inventory Rebirth;
    public static Inventory Befehle;
    public static Inventory Auswahl;
    public static Inventory Verwaltung;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;
        if (mainGUI == null || Auswahl == null || Einstellungen == null || Rebirth == null || Befehle == null || Verwaltung == null) {
            createguis(player);
        }
        player.openInventory(mainGUI);
        return true;
    }

    private void createguis(Player player) {

        Einstellungen = Bukkit.createInventory(null, 9, "§cInsel-Einstellungen");
        Rebirth = Bukkit.createInventory(null, 9, "§eRebirth");
        Befehle = Bukkit.createInventory(null, 9, "§8Spielerbefehle");
        Auswahl = Bukkit.createInventory(null, 9, "§aPhasen-Auswahl");
        Verwaltung = Bukkit.createInventory(null, 9, "§cInsel-Verwaltung");
        mainGUI = Bukkit.createInventory(null, 9, "§8OneBlock-Menü");

        for (int pos : grayglasmaingui) {
            mainGUI.setItem(pos, new ItemStack(GRAY_STAINED_GLASS_PANE));
        }

        // Slot 0 - Repeater
        ItemStack repeaterLeft = new ItemStack(REPEATER);
        ItemMeta meta0 = repeaterLeft.getItemMeta();
        if (meta0 != null) {
            meta0.setDisplayName("§cInsel-Einstellungen");
            repeaterLeft.setItemMeta(meta0);
        }
        mainGUI.setItem(2, repeaterLeft);

        // Slot 2 - Totem
        ItemStack totem = new ItemStack(TOTEM_OF_UNDYING);
        ItemMeta meta2 = totem.getItemMeta();
        if (meta2 != null) {
            meta2.setDisplayName("§6Insel-Rebirth");
            totem.setItemMeta(meta2);
        }
        mainGUI.setItem(3, totem);

        // Slot 4 - Spielerkopf
        ItemStack skull = new ItemStack(PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName("§eBefehle");
            skull.setItemMeta(skullMeta);
        }
        mainGUI.setItem(4, skull);

        // Slot 6 - XP-Flasche
        ItemStack xpBottle = new ItemStack(EXPERIENCE_BOTTLE);
        ItemMeta meta6 = xpBottle.getItemMeta();
        if (meta6 != null) {
            meta6.setDisplayName("§aPhasen-Auswahl");
            xpBottle.setItemMeta(meta6);
        }
        mainGUI.setItem(5, xpBottle);

        // Slot 8 - Repeater
        ItemStack repeaterRight = new ItemStack(COMPARATOR);
        ItemMeta meta8 = repeaterRight.getItemMeta();
        if (meta8 != null) {
            meta8.setDisplayName("§cInsel-Verwaltung");
            repeaterRight.setItemMeta(meta8);
        }
        mainGUI.setItem(6, repeaterRight);

    }

    private void openUpgradeShop(Player player) {
        upgradeShop = Bukkit.createInventory(null, 6 * 9, "Upgrade-Shop");

        YamlConfiguration config = Manager.getIslandConfig(player);
        int currentSize = config.getInt("WorldBorderSize", 50);
        int costLevel = ((currentSize - 40) / 10) + 1;
        int neededLevel = costLevel * 2;
        int playerLevel = config.getInt("IslandLevel", 1);

        ItemStack upgradeItem = new ItemStack(STRUCTURE_VOID);
        ItemMeta meta = upgradeItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aWorldBorder vergrößern!");
            List<String> lore = new ArrayList<>();
            lore.add("§7Aktuelle Größe: §e" + currentSize);
            lore.add("§7Kosten: §e10 Tokens");
            lore.add("§7Klicke, um deine Border zu erweitern!");
            if (playerLevel >= neededLevel) {
                lore.add("§aDu kannst upgraden!");
            } else {
                lore.add("§cDu benötigst ein höheres Level.");
            }
            meta.setLore(lore);
            upgradeItem.setItemMeta(meta);
        }

        upgradeShop.setItem(20, upgradeItem);
        player.openInventory(upgradeShop);

        ItemStack rebirth = new ItemStack(TOTEM_OF_UNDYING);
        ItemMeta rebirthMeta = rebirth.getItemMeta();
        if (rebirthMeta != null) {
            rebirthMeta.setDisplayName("§6Rebirth kommt bald...");
            rebirth.setItemMeta(rebirthMeta);
            upgradeShop.setItem(22, rebirth);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();
        Material type = clicked.getType();

        if (title.equalsIgnoreCase("OneBlock Menü")) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

            switch (type) {
                case EXPERIENCE_BOTTLE:
                    player.openInventory(Auswahl);
                    break;
                case REPEATER:
                    player.openInventory(Einstellungen);
                    break;
                case TOTEM_OF_UNDYING:
                    player.openInventory(Rebirth);
                    break;
                case PLAYER_HEAD:
                    player.openInventory(Befehle);
                    break;
                case COMPARATOR:
                    player.openInventory(Verwaltung);
                    break;
            }
        }

        if (title.equalsIgnoreCase("Upgrade-Shop")) {
            event.setCancelled(true);

            if (type == STRUCTURE_VOID) {
                YamlConfiguration config = Manager.getIslandConfig(player);
                int currentSize = config.getInt("WorldBorderSize", 50);

                if (currentSize < 200) {
                    currentSize += 10;
                    config.set("WorldBorderSize", currentSize);
                    Manager.saveIslandConfig(player, config);

                    WorldBorder border = player.getWorld().getWorldBorder();
                    border.setCenter(player.getLocation());
                    border.setSize(currentSize);
                    Main.setWorldBorder(player);

                    player.sendMessage("§aDeine WorldBorder wurde auf §e" + currentSize + " §avergrößert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    player.closeInventory();
                } else {
                    player.sendMessage("§cDu hast das Limit erreicht");
                }
            }
        }
    }
}