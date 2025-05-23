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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import javax.naming.Name;
import java.util.*;

import static de.Main.OneBlock.Manager.*;
import static org.bukkit.Material.*;

public class OBGUI implements CommandExecutor, Listener {

    private final int[] grayglasmaingui = {0, 1, 2, 6, 7, 8};
    private final int[] grayglasmaingui2 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26};
    private final int[] grayglasmaingui3 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 22, 23, 24, 25, 26, 28, 29, 30, 31, 32, 33, 34, 35};
    private final int[] grayglasmaingui4 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 20, 22, 23, 24, 25, 26, 28, 29, 30, 31, 32, 33, 34, 35};

    private final int MAX_CLICKS = 3;

    public static Inventory upgradeShop;
    public static Inventory mainGUI;
    public static Inventory Einstellungen;
    public static Inventory Rebirth;
    public static Inventory Befehle;
    public static Inventory Auswahl;
    public static Inventory Verwaltung;

    private final HashMap<UUID, Integer> deleteClicks = new HashMap<>();
    private final HashMap<UUID, Integer> rebirthClicks = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        if (mainGUI == null || Auswahl == null || Einstellungen == null || Rebirth == null || Befehle == null || Verwaltung == null) {
            createguis(player);
        }

        deleteClicks.put(player.getUniqueId(), MAX_CLICKS);
        rebirthClicks.put(player.getUniqueId(), MAX_CLICKS);

        updateVerwaltungGUI(player);
        player.openInventory(mainGUI);
        return true;
    }

    private void createguis(Player player) {
        YamlConfiguration config = getIslandConfig(player.getUniqueId());

        Einstellungen = Bukkit.createInventory(null, 3 * 9, "§cInsel-Einstellungen");
        Rebirth = Bukkit.createInventory(null, 3 * 9, "§eRebirth");
        Befehle = Bukkit.createInventory(null, 4 * 9, "§8Spielerbefehle");
        Auswahl = Bukkit.createInventory(null, 4 * 9, "§aPhasen-Auswahl");
        Verwaltung = Bukkit.createInventory(null, 3 * 9, "§cInsel-Verwaltung");
        mainGUI = Bukkit.createInventory(null, 9, "§aOneBlock-Menü");

        for (int pos : grayglasmaingui) {
            mainGUI.setItem(pos, new ItemStack(GRAY_STAINED_GLASS_PANE));
        }

        for (int pos : grayglasmaingui2) {
            Verwaltung.setItem(pos, new ItemStack(GRAY_STAINED_GLASS_PANE));
        }

        for (int pos : grayglasmaingui3) {
            Auswahl.setItem(pos, new ItemStack(GRAY_STAINED_GLASS_PANE));

        }

        for (int pos : grayglasmaingui4) {
            Befehle.setItem(pos, new ItemStack(GRAY_STAINED_GLASS_PANE));
        }

        setPlayerHeadInMainGUI(player);


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
        mainGUI.setItem(3, xpBottle);


        ItemStack comparator = new ItemStack(COMPARATOR);
        ItemMeta meta8 = comparator.getItemMeta();
        if (meta8 != null) {
            meta8.setDisplayName("§cInsel-Verwaltung");
            List<String> lore = new ArrayList<>();

            lore.add(" ");
            lore.add("§fKlicke um in das Insel-Verwaltung-Menü zu gelangen!");
            lore.add(" ");

            meta8.setLore(lore);
            comparator.setItemMeta(meta8);
        }
        mainGUI.setItem(5, comparator);



        ItemStack auswahl = new ItemStack(GRASS_BLOCK);
        ItemMeta auswahlmeta = auswahl.getItemMeta();
        if (auswahlmeta != null) {
            String name = Main.config.getString("oneblockblocks." + 1 + ".name", "Unbekannt");
            auswahlmeta.setDisplayName("§bPhase: §a" + name);
            auswahl.setItemMeta(auswahlmeta);
        }
        Auswahl.setItem(10, auswahl);

        ItemStack auswahl1 = new ItemStack(OAK_LOG);
        ItemMeta auswahlmeta1 = auswahl1.getItemMeta();
        if (auswahlmeta1 != null) {
            String name = Main.config.getString("oneblockblocks." + 2 + ".name", "Unbekannt");
            auswahlmeta1.setDisplayName("§bPhase: §a" + name);
            auswahl1.setItemMeta(auswahlmeta1);
        }
        Auswahl.setItem(11, auswahl1);

        ItemStack auswahl2 = new ItemStack(STONE);
        ItemMeta auswahlmeta2 = auswahl2.getItemMeta();
        if (auswahlmeta2 != null) {
            String name = Main.config.getString("oneblockblocks." + 3 + ".name", "Unbekannt");
            auswahlmeta2.setDisplayName("§bPhase: §a" + name);
            auswahl2.setItemMeta(auswahlmeta2);
        }
        Auswahl.setItem(12, auswahl2);

        ItemStack auswahl3 = new ItemStack(IRON_ORE);
        ItemMeta auswahlmeta3 = auswahl3.getItemMeta();
        if (auswahlmeta3 != null) {
            String name = Main.config.getString("oneblockblocks." + 4 + ".name", "Unbekannt");
            auswahlmeta3.setDisplayName("§bPhase: §a" + name);
            auswahl3.setItemMeta(auswahlmeta3);
        }
        Auswahl.setItem(13, auswahl3);

        ItemStack auswahl4 = new ItemStack(DIAMOND_BLOCK);
        ItemMeta auswahlmeta4 = auswahl4.getItemMeta();
        if (auswahlmeta4 != null) {
            String name = Main.config.getString("oneblockblocks." + 5 + ".name", "Unbekannt");
            auswahlmeta4.setDisplayName("§bPhase: §a" + name);
            auswahl4.setItemMeta(auswahlmeta4);
        }
        Auswahl.setItem(14, auswahl4);

        ItemStack auswahl5 = new ItemStack(NETHERRACK);
        ItemMeta auswahlmeta5 = auswahl5.getItemMeta();
        if (auswahlmeta5 != null) {
            String name = Main.config.getString("oneblockblocks." + 6 + ".name", "Unbekannt");
            auswahlmeta5.setDisplayName("§bPhase: §a" + name);
            auswahl5.setItemMeta(auswahlmeta5);
        }
        Auswahl.setItem(15, auswahl5);

        ItemStack auswahl6 = new ItemStack(WARPED_STEM);
        ItemMeta auswahlmeta6 = auswahl6.getItemMeta();
        if (auswahlmeta6 != null) {
            String name = Main.config.getString("oneblockblocks." + 7 + ".name", "Unbekannt");
            auswahlmeta6.setDisplayName("§bPhase: §a" + name);
            auswahl6.setItemMeta(auswahlmeta6);
        }
        Auswahl.setItem(16, auswahl6);

        ItemStack auswahl7 = new ItemStack(END_STONE);
        ItemMeta auswahlmeta7 = auswahl7.getItemMeta();
        if (auswahlmeta7 != null) {
            String name = Main.config.getString("oneblockblocks." + 8 + ".name", "Unbekannt");
            auswahlmeta7.setDisplayName("§bPhase: §a" + name);
            auswahl7.setItemMeta(auswahlmeta7);
        }
        Auswahl.setItem(19, auswahl7);

        ItemStack auswahl8 = new ItemStack(NETHERITE_BLOCK);
        ItemMeta auswahlmeta8 = auswahl8.getItemMeta();
        if (auswahlmeta8 != null) {
            String name = Main.config.getString("oneblockblocks." + 9 + ".name", "Unbekannt");
            auswahlmeta8.setDisplayName("§bPhase: §a" + name);
            auswahl8.setItemMeta(auswahlmeta8);
        }
        Auswahl.setItem(20, auswahl8);

        ItemStack auswahl9 = new ItemStack(BEACON);
        ItemMeta auswahlmeta9 = auswahl9.getItemMeta();
        if (auswahlmeta9 != null) {
            String name = Main.config.getString("oneblockblocks." + 10 + ".name", "Unbekannt");
            auswahlmeta9.setDisplayName("§bPhase: §a" + name);
            auswahl9.setItemMeta(auswahlmeta9);
        }
        Auswahl.setItem(21, auswahl9);

        ItemStack zurück = new ItemStack(RED_DYE);
        ItemMeta zurückmeta = zurück.getItemMeta();
        if (zurückmeta != null) {
            zurückmeta.setDisplayName("§cZurück zum §aOneBlock-Menü");
            zurück.setItemMeta(zurückmeta);
        }
        Auswahl.setItem(27, zurück);




        ItemStack befehl = new ItemStack(GRASS_BLOCK);
        ItemMeta befehlmeta = befehl.getItemMeta();
        if (befehlmeta != null) {
            befehlmeta.setDisplayName("§a/ob join");
            befehl.setItemMeta(befehlmeta);
        }
        Befehle.setItem(10, befehl);

        ItemStack befehl2 = new ItemStack(NAME_TAG);
        ItemMeta befehlmeta2 = befehl2.getItemMeta();
        if (befehlmeta2 != null) {
            befehlmeta2.setDisplayName("§a/ob trust");
            befehl2.setItemMeta(befehlmeta2);
        }
        Befehle.setItem(11, befehl2);

        ItemStack befehl3 = new ItemStack(GREEN_DYE);
        ItemMeta befehlmeta3 = befehl3.getItemMeta();
        if (befehlmeta3 != null) {
            befehlmeta3.setDisplayName("§a/ob accept");
            befehl3.setItemMeta(befehlmeta3);
        }
        Befehle.setItem(12, befehl3);

        ItemStack befehl4 = new ItemStack(BARRIER);
        ItemMeta befehlmeta4 = befehl4.getItemMeta();
        if (befehlmeta4 != null) {
            befehlmeta4.setDisplayName("§a/ob decline");
            befehl4.setItemMeta(befehlmeta4);
        }
        Befehle.setItem(13, befehl4);

        ItemStack befehl5 = new ItemStack(DARK_OAK_DOOR);
        ItemMeta befehlmeta5 = befehl5.getItemMeta();
        if (befehlmeta5 != null) {
            befehlmeta5.setDisplayName("§a/ob leave");
            befehl5.setItemMeta(befehlmeta5);
        }
        Befehle.setItem(14, befehl5);

        ItemStack befehl6 = new ItemStack(TNT);
        ItemMeta befehlmeta6 = befehl6.getItemMeta();
        if (befehlmeta6 != null) {
            befehlmeta6.setDisplayName("§a/ob remove");
            befehl6.setItemMeta(befehlmeta6);
        }
        Befehle.setItem(15, befehl6);

        ItemStack befehl7 = new ItemStack(RED_STAINED_GLASS_PANE);
        ItemMeta befehlmeta7 = befehl7.getItemMeta();
        if (befehlmeta7 != null) {
            befehlmeta7.setDisplayName("§a/ob deny");
            befehl7.setItemMeta(befehlmeta7);
        }
        Befehle.setItem(16, befehl7);

        ItemStack befehl8 = new ItemStack(LIME_DYE);
        ItemMeta befehlmeta8 = befehl8.getItemMeta();
        if (befehlmeta8 != null) {
            befehlmeta8.setDisplayName("§a/ob unban");
            befehl8.setItemMeta(befehlmeta8);
        }
        Befehle.setItem(19, befehl8);

        ItemStack befehl9 = new ItemStack(ENDER_PEARL);
        ItemMeta befehlmeta9 = befehl9.getItemMeta();
        if (befehlmeta9 != null) {
            befehlmeta9.setDisplayName("§a/ob visit");
            befehl9.setItemMeta(befehlmeta9);
        }
        Befehle.setItem(20, befehl9);

        ItemStack zurück1 = new ItemStack(RED_DYE);
        ItemMeta zurückmeta1 = zurück1.getItemMeta();
        if (zurückmeta1 != null) {
            zurückmeta1.setDisplayName("§cZurück zum §aOneBlock-Menü");
            zurück1.setItemMeta(zurückmeta1);
        }
        Befehle.setItem(27, zurück1);


    }

    private void setPlayerHeadInMainGUI(Player player) {
        YamlConfiguration config = getIslandConfig(player.getUniqueId());
        ItemStack skull = new ItemStack(PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName("§eBefehle");
            List<String> lore = new ArrayList<>();

            lore.add(" ");
            lore.add("§fDein Profil:");
            lore.add(" ");

            lore.add("§fKlicke, dass du zu den Spielerbefehle-Menü zu gelangst!");
            lore.add(" ");

            boolean hatInsel = config.getBoolean("EigeneInsel", false);

            if (hatInsel) {

                lore.add("§cDein OneBlock Level: " + "§b" + config.getInt("IslandLevel"));


                int x = config.getInt("OneBlock-x");
                int z = config.getInt("OneBlock-z");

                int Missing =config.getInt("MissingBlocksToLevelUp");
                int total =config.getInt("TotalBlocks");

                lore.add("§cDein OneBlock Standort: " + "§b" + "X: " + x + ", Z: " + z);
                lore.add("§cVerbleibende Blöcke und gesamte Blöcke: " + "§b"+ Missing +  " | " + total);
                lore.add("§cDeine Worldborder größe: " + "§b" + + config.getInt("WorldBorderSize"));
            } else {
                lore.add("§bDu besitzt §ckeine Insel!");
                lore.add("§bBitte §cerstelle §bdir eine Insel.");

            }

            skullMeta.setLore(lore);
            skull.setItemMeta(skullMeta);

            saveIslandConfig(player.getUniqueId(), config);
        }
        mainGUI.setItem(4, skull);
    }





    private void updateVerwaltungGUI(Player player) {
        UUID uuid = player.getUniqueId();
        int deleteRemaining = deleteClicks.getOrDefault(uuid, MAX_CLICKS);
        int rebirthRemaining = rebirthClicks.getOrDefault(uuid, MAX_CLICKS);

        for (int slot : new int[]{11, 13, 15}) {
            ItemStack item;
            String displayName;
            List<String> lore = new ArrayList<>();

            switch (slot) {
                case 11 -> { 
                    item = new ItemStack(BARRIER);
                    displayName = "§cInsel-Löschen";
                    lore.add(" ");
                    lore.add("§cInsel Löschung nicht mehr rückgängig!");
                    lore.add(" ");
                    lore.add("§7Klicke §e" + deleteRemaining + " §7Mal zum §cLöschen§7!");
                }
                case 13 -> { 
                    item = new ItemStack(TOTEM_OF_UNDYING);
                    displayName = "§cRebirth";
                    lore.add(" ");
                    lore.add("§bDeine OneBlock wird wieder auf §4Level 1 §bgesetzt!");
                    lore.add("§bAber du bekommst Belohnungen für den §cRebirth");
                    lore.add(" ");
                    lore.add("§7Klicke §e" + rebirthRemaining + " §7Mal zum §cRebirth§7!");
                }
                case 15 -> {
                    item = new ItemStack(STRUCTURE_VOID);
                    displayName = "§aWorldBorder Größe";
                    YamlConfiguration config = getIslandConfig(player.getUniqueId());
                    int currentSize = config.getInt("WorldBorderSize", 50);

                    int basePrice = 100;
                    int upgradesDone = (currentSize - 50) / 10; 
                    int price = (int) (basePrice * Math.pow(2, upgradesDone)); 

                    lore.add("§7Aktuelle Größe: §e" + currentSize);
                    lore.add("§7Klicke, um die Größe zu erhöhen");
                    lore.add("§7(Maximal 200)");
                    lore.add("§7Preis für nächstes Upgrade: §e" + price + " Coins");
                }

                default -> {
                    continue;
                }
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(displayName);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            Verwaltung.setItem(slot, item);


            ItemStack zurück = new ItemStack(RED_DYE);
            ItemMeta zurückmeta = zurück.getItemMeta();
            if (zurückmeta != null) {
                zurückmeta.setDisplayName("§cZurück zum §aOneBlock-Menü");
                zurück.setItemMeta(zurückmeta);
            }
            Verwaltung.setItem(18, zurück);
        }
    }

    private void updateDeleteItemLore(Player player, int remainingClicks) {
        UUID uuid = player.getUniqueId();
        int deleteRemaining = deleteClicks.getOrDefault(uuid, MAX_CLICKS);
        int rebirthRemaining = rebirthClicks.getOrDefault(uuid, MAX_CLICKS);

        Inventory inv = player.getOpenInventory().getTopInventory();
        ItemStack item = inv.getItem(11);
        if (item == null || item.getType() != BARRIER) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = new ArrayList<>();
        if (remainingClicks > 0) {
            lore.add(" ");
            lore.add("§cInsel Löschung nicht mehr rückgängig!");
            lore.add(" ");
            lore.add("§7Klicke §e" + deleteRemaining + " §7Mal zum §cLöschen§7!");
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
        if (item == null || item.getType() != TOTEM_OF_UNDYING) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = new ArrayList<>();
        if (remainingClicks > 0) {
            lore.add(" ");
            lore.add("§bDeine OneBlock wird wieder auf §4Level 1 §bgesetzt!");
            lore.add("§bAber du bekommst Belohnungen für den §cRebirth");
            lore.add(" ");
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

        UUID uuid = player.getUniqueId();
        YamlConfiguration config = getIslandConfig(uuid);

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();
        Material type = clicked.getType();


        if (title.equalsIgnoreCase("§cInsel-Verwaltung")) {
            event.setCancelled(true);

            switch (type) {

                case STRUCTURE_VOID -> {
                    addWorldBoarder(config, player);
                }

                case TOTEM_OF_UNDYING -> {
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
                case BARRIER -> {
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
                case RED_DYE -> {
                    player.openInventory(mainGUI);
                }
                default -> {
                  
                }
            }
        }

        if (title.equalsIgnoreCase("§aOneBlock-Menü")) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

            switch (type) {
                case EXPERIENCE_BOTTLE -> player.openInventory(Auswahl);
                case PLAYER_HEAD -> player.openInventory(Befehle);
                case COMPARATOR -> {
                    deleteClicks.put(uuid, MAX_CLICKS);
                    rebirthClicks.put(uuid, MAX_CLICKS);
                    updateVerwaltungGUI(player);
                    player.openInventory(Verwaltung);
                }
                default -> {
                  
                }
            }
        }
        if (title.equalsIgnoreCase("§aPhasen-Auswahl")) {
            event.setCancelled(true);


            boolean durchgespielt = config.getBoolean("Durchgespielt");

            switch (type) {
                case GRASS_BLOCK:

                    if (durchgespielt) {
                        config.set("IslandLevel", 1);
                        config.set("MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.1.blockcount"));
                        config.set("TotalBlocks", Main.config.getInt("oneblockblocks.1.blockcount"));
                        config.set("Durchgespielt", true);
                        Manager.saveIslandConfig(player.getUniqueId(), config);

                        player.sendMessage("§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 1 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case OAK_LOG:

                    if (durchgespielt) {
                        config.set("IslandLevel", 2);
                        config.set("MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.2.blockcount"));
                        config.set("TotalBlocks", Main.config.getInt("oneblockblocks.2.blockcount"));
                        config.set("Durchgespielt", true);
                        Manager.saveIslandConfig(player.getUniqueId(), config);

                        player.sendMessage("§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 2 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case STONE:

                    if (durchgespielt) {
                        config.set("IslandLevel", 3);
                        config.set("MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.3.blockcount"));
                        config.set("TotalBlocks", Main.config.getInt("oneblockblocks.3.blockcount"));
                        config.set("Durchgespielt", true);
                        Manager.saveIslandConfig(player.getUniqueId(), config);

                        player.sendMessage("§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 3 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case IRON_ORE:

                    if (durchgespielt) {
                        config.set("IslandLevel", 4);
                        config.set("MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.4.blockcount"));
                        config.set("TotalBlocks", Main.config.getInt("oneblockblocks.4.blockcount"));
                        config.set("Durchgespielt", true);
                        Manager.saveIslandConfig(player.getUniqueId(), config);

                        player.sendMessage("§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 4 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case DIAMOND_BLOCK:

                    if (durchgespielt) {
                        config.set("IslandLevel", 5);
                        config.set("MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.5.blockcount"));
                        config.set("TotalBlocks", Main.config.getInt("oneblockblocks.5.blockcount"));
                        config.set("Durchgespielt", true);
                        Manager.saveIslandConfig(player.getUniqueId(), config);

                        player.sendMessage("§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 5 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case NETHERRACK:

                    if (durchgespielt) {
                        config.set("IslandLevel", 6);
                        config.set("MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.6.blockcount"));
                        config.set("TotalBlocks", Main.config.getInt("oneblockblocks.6.blockcount"));
                        config.set("Durchgespielt", true);
                        Manager.saveIslandConfig(player.getUniqueId(), config);

                        player.sendMessage("§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 6 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case WARPED_STEM:

                    if (durchgespielt) {
                        config.set("IslandLevel", 7);
                        config.set("MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.7.blockcount"));
                        config.set("TotalBlocks", Main.config.getInt("oneblockblocks.7.blockcount"));
                        config.set("Durchgespielt", true);
                        Manager.saveIslandConfig(player.getUniqueId(), config);

                        player.sendMessage("§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 7 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case END_STONE:

                    if (durchgespielt) {
                        config.set("IslandLevel", 8);
                        config.set("MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.8.blockcount"));
                        config.set("TotalBlocks", Main.config.getInt("oneblockblocks.8.blockcount"));
                        config.set("Durchgespielt", true);
                        Manager.saveIslandConfig(player.getUniqueId(), config);

                        player.sendMessage("§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 8 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case NETHERITE_BLOCK:

                    if (durchgespielt) {
                        config.set("IslandLevel", 9);
                        config.set("MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.9.blockcount"));
                        config.set("TotalBlocks", Main.config.getInt("oneblockblocks.9.blockcount"));
                        config.set("Durchgespielt", true);
                        Manager.saveIslandConfig(player.getUniqueId(), config);

                        player.sendMessage("§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 9 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case BEACON:

                    if (durchgespielt) {
                        config.set("IslandLevel", 10);
                        config.set("MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.10.blockcount"));
                        config.set("TotalBlocks", Main.config.getInt("oneblockblocks.10.blockcount"));
                        config.set("Durchgespielt", true);
                        Manager.saveIslandConfig(player.getUniqueId(), config);

                        player.sendMessage("§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 10 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case RED_DYE:
                    player.openInventory(mainGUI);
                    break;
            }
        }

        if (title.equalsIgnoreCase("§8Spielerbefehle")) {
            event.setCancelled(true);

            switch (type) {
                case GRASS_BLOCK:
                    player.performCommand("ob join");
                    player.closeInventory();
                    break;

                case NAME_TAG:
                    player.closeInventory();
                    sendSuggestCommandMessage(player, "trust");
                    break;

                case GREEN_DYE:
                    player.performCommand("ob accept");
                    player.closeInventory();
                    break;

                case BARRIER:
                    player.closeInventory();
                    sendSuggestCommandMessage(player, "decline");
                    break;

                case DARK_OAK_DOOR:
                    player.closeInventory();
                    sendSuggestCommandMessage(player, "leave");
                    break;

                case TNT:
                    player.closeInventory();
                    sendSuggestCommandMessage(player, "remove");
                    break;

                case RED_STAINED_GLASS_PANE:
                    player.closeInventory();
                    sendSuggestCommandMessage(player, "deny");
                    break;

                case LIME_DYE:
                    player.closeInventory();
                    sendSuggestCommandMessage(player, "unban");
                    break;

                case ENDER_PEARL:
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    sendSuggestCommandMessage(player, "visit ");
                    break;


                case RED_DYE:
                    player.openInventory(mainGUI);
                    break;

                default:
                    break;
            }

        }
    }

    private void sendSuggestCommandMessage(Player player, String command) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

        TextComponent msg = new TextComponent("§aKlicke hier, um den Befehl einzugeben: ");
        TextComponent commandPart = new TextComponent("§e/ob " + command);

        commandPart.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ob " + command));
        msg.addExtra(commandPart);

        player.spigot().sendMessage(msg);
    }



    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
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
            UUID uuid = player.getUniqueId();
            deleteClicks.put(uuid, MAX_CLICKS);
            rebirthClicks.put(uuid, MAX_CLICKS);
            updateVerwaltungGUI(player);
        }

        if (title.equalsIgnoreCase("§aOneBlock-Menü")) {
          
            setPlayerHeadInMainGUI(player);
            updateVerwaltungGUI(player);
        }
    }
    public static void addWorldBoarder (YamlConfiguration config, Player player){

        UUID uuid = player.getUniqueId();

        int currentSize = config.getInt("WorldBorderSize", 50); 
        int maxSize = 200;

        if (currentSize >= maxSize) {
            player.sendMessage("§cDu hast das Limit erreicht");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
            return;
        }

        int basePrice = 20000;
        int upgradesDone = (currentSize - 50) / 10; 
        int price = (int) (basePrice * Math.pow(2, upgradesDone)); 

        if (economy.getBalance(player) < price) {
            player.sendMessage("§cDu hast nicht genug Geld! Benötigt: §e" + price + " Coins");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

   
        economy.withdrawPlayer(player, price);

      
        currentSize += 10;
        config.set("WorldBorderSize", currentSize);
        saveIslandConfig(uuid, config);

        player.sendMessage("§aDeine WorldBorder wurde auf §e" + currentSize + " §avergrößert!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        player.closeInventory();

      
    }

}