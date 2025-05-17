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
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

import static org.bukkit.Material.*;

public class OBGUI implements CommandExecutor, Listener {

    int[] grayglasmaingui = {0, 1, 2, 6, 7, 8};

    public static Inventory upgradeShop;
    public static Inventory mainGUI;
    public static Inventory Einstellungen;
    public static Inventory Rebirth;
    public static Inventory Befehle;
    public static Inventory Auswahl;
    public static Inventory Verwaltung;

    // Getrennte Maps für Klicks
    private final HashMap<UUID, Integer> deleteClicks = new HashMap<>();
    private final HashMap<UUID, Integer> rebirthClicks = new HashMap<>();

    private final int MAX_CLICKS = 3;

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

        // Klick-Zähler beim Öffnen zurücksetzen
        deleteClicks.put(player.getUniqueId(), MAX_CLICKS);
        rebirthClicks.put(player.getUniqueId(), MAX_CLICKS);

        updateVerwaltungGUI(player);
        player.openInventory(mainGUI);
        return true;
    }

    private void createguis(Player player) {

        Einstellungen = Bukkit.createInventory(null, 3 * 9, "§cInsel-Einstellungen");
        Rebirth = Bukkit.createInventory(null, 3 * 9, "§eRebirth");
        Befehle = Bukkit.createInventory(null, 3 * 9, "§8Spielerbefehle");
        Auswahl = Bukkit.createInventory(null, 3 * 9, "§aPhasen-Auswahl");
        Verwaltung = Bukkit.createInventory(null, 3 * 9, "§cInsel-Verwaltung");
        mainGUI = Bukkit.createInventory(null, 9, "§8OneBlock-Menü");

        for (int pos : grayglasmaingui) {
            mainGUI.setItem(pos, new ItemStack(GRAY_STAINED_GLASS_PANE));
        }

        ItemStack skull = new ItemStack(PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName("§eBefehle");
            skull.setItemMeta(skullMeta);
        }
        mainGUI.setItem(4, skull);

        ItemStack xpBottle = new ItemStack(EXPERIENCE_BOTTLE);
        ItemMeta meta6 = xpBottle.getItemMeta();
        if (meta6 != null) {
            meta6.setDisplayName("§aPhasen-Auswahl");
            xpBottle.setItemMeta(meta6);
        }
        mainGUI.setItem(3, xpBottle);

        ItemStack repeaterRight = new ItemStack(COMPARATOR);
        ItemMeta meta8 = repeaterRight.getItemMeta();
        if (meta8 != null) {
            meta8.setDisplayName("§cInsel-Verwaltung");
            repeaterRight.setItemMeta(meta8);
        }
        mainGUI.setItem(5, repeaterRight);

        // Rebirth Item in Verwaltung initial setzen
        ItemStack rebirth = new ItemStack(TOTEM_OF_UNDYING);
        ItemMeta rebirthmeta = rebirth.getItemMeta();
        if (rebirthmeta != null) {
            rebirthmeta.setDisplayName("§cRebirth");
            List<String> lore = new ArrayList<>();
            lore.add("§bDeine OneBlock wird wieder auf §4Level 1 §bgesetzt!");
            lore.add("§bDu bekommst aber Belohnungen für den §cRebirth");
            rebirthmeta.setLore(lore);
            rebirth.setItemMeta(rebirthmeta);
        }
        Verwaltung.setItem(13, rebirth);

        updateVerwaltungGUI(player);
    }

    private void updateVerwaltungGUI(Player player) {
        UUID uuid = player.getUniqueId();

        int deleteRemaining = deleteClicks.getOrDefault(uuid, MAX_CLICKS);
        int rebirthRemaining = rebirthClicks.getOrDefault(uuid, MAX_CLICKS);

        // Delete Item
        ItemStack deleteItem = new ItemStack(BARRIER);
        ItemMeta metaDelete = deleteItem.getItemMeta();
        if (metaDelete != null) {
            metaDelete.setDisplayName("§cInsel-Löschen");
            List<String> lore = new ArrayList<>();
            lore.add("§7Klicke §e" + deleteRemaining + " §7Mal zum §cLöschen§7!");
            metaDelete.setLore(lore);
            deleteItem.setItemMeta(metaDelete);
        }
        Verwaltung.setItem(11, deleteItem);

        // Rebirth Item
        ItemStack rebirthItem = new ItemStack(TOTEM_OF_UNDYING);
        ItemMeta metaRebirth = rebirthItem.getItemMeta();
        if (metaRebirth != null) {
            metaRebirth.setDisplayName("§cRebirth");
            List<String> lore = new ArrayList<>();
            lore.add("§7Klicke §e" + rebirthRemaining + " §7Mal zum §cRebirth§7!");
            metaRebirth.setLore(lore);
            rebirthItem.setItemMeta(metaRebirth);
        }
        Verwaltung.setItem(13, rebirthItem);
    }

    private void updateDeleteItemLore(Player player, int remainingClicks) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        ItemStack item = inv.getItem(11);
        if (item == null || item.getType() != Material.BARRIER) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = new ArrayList<>();
        if (remainingClicks > 0) {
            lore.add("§7Klicke §e" + remainingClicks + " §7weitere Male zum §cLöschen§7!");
        } else {
            lore.add("§cLösche wird ausgeführt...");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(11, item);
    }

    private void updateRebirthItemLore(Player player, int remainingClicks) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        ItemStack item = inv.getItem(13);
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = new ArrayList<>();
        if (remainingClicks > 0) {
            lore.add("§7Klicke §e" + remainingClicks + " §7weitere Male zum §cRebirth§7!");
        } else {
            lore.add("§cRebirth wird ausgeführt...");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(13, item);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();
        Material type = clicked.getType();

        if (title.equalsIgnoreCase("§cInsel-Verwaltung")) {
            event.setCancelled(true);

            if (type == STRUCTURE_VOID) {
                YamlConfiguration config = Manager.getIslandConfig(player.getUniqueId());
                int currentSize = config.getInt("WorldBorderSize", 50);

                if (currentSize < 200) {
                    currentSize += 10;
                    config.set("WorldBorderSize", currentSize);
                    Manager.saveIslandConfig(player.getUniqueId(), config);

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

            if (type == TOTEM_OF_UNDYING) {
                UUID uuid = player.getUniqueId();
                int clicksLeft = rebirthClicks.getOrDefault(uuid, MAX_CLICKS) - 1;

                if (clicksLeft > 0) {
                    rebirthClicks.put(uuid, clicksLeft);
                    updateRebirthItemLore(player, clicksLeft);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                } else {
                    rebirthClicks.remove(uuid);
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                    player.performCommand("ob rebirth");
                }
            }

            if (type == BARRIER) {
                UUID uuid = player.getUniqueId();
                int clicksLeft = deleteClicks.getOrDefault(uuid, MAX_CLICKS) - 1;

                if (clicksLeft > 0) {
                    deleteClicks.put(uuid, clicksLeft);
                    updateDeleteItemLore(player, clicksLeft);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                } else {
                    deleteClicks.remove(uuid);
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                    player.performCommand("ob delete");
                }
            }
        }

        if (title.equalsIgnoreCase("§8OneBlock-Menü")) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

            switch (type) {
                case EXPERIENCE_BOTTLE:
                    player.openInventory(Auswahl);
                    break;
                case PLAYER_HEAD:
                    player.openInventory(Befehle);
                    break;
                case COMPARATOR:
                    // Verwaltung mit frisch zurückgesetzten Klicks öffnen
                    UUID uuid = player.getUniqueId();
                    deleteClicks.put(uuid, MAX_CLICKS);
                    rebirthClicks.put(uuid, MAX_CLICKS);
                    updateVerwaltungGUI(player);
                    player.openInventory(Verwaltung);
                    break;
            }
        }
    }

    // Klick-Zähler zurücksetzen, wenn Inventar geschlossen wird
    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = event.getView().getTitle();

        if (title.equalsIgnoreCase("§cInsel-Verwaltung")) {
            UUID uuid = player.getUniqueId();
            deleteClicks.remove(uuid);
            rebirthClicks.remove(uuid);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = event.getView().getTitle();

        if (title.equalsIgnoreCase("§cInsel-Verwaltung")) {
            // Klick-Zähler zurücksetzen, wenn das Verwaltung-GUI geöffnet wird
            UUID uuid = player.getUniqueId();
            deleteClicks.put(uuid, MAX_CLICKS);
            rebirthClicks.put(uuid, MAX_CLICKS);
            updateVerwaltungGUI(player);
        }

        if (title.equalsIgnoreCase("§8OneBlock-Menü")) {
            updateVerwaltungGUI(player);
        }
    }
}
