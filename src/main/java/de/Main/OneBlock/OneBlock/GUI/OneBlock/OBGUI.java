package de.Main.OneBlock.OneBlock.GUI.OneBlock;

import de.Main.OneBlock.Main;
import de.Main.OneBlock.OneBlock.Manager.OneBlockManager;
import de.Main.OneBlock.database.DBM;
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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

import static de.Main.OneBlock.OneBlock.Manager.Manager.*;
import static org.bukkit.Material.*;

public class OBGUI implements CommandExecutor, Listener {

    private final int[] grayglasmaingui = {0, 1, 2, 6, 7, 8};
    private final int[] verwaltungGrayGlasPane = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 23, 25, 26, 28, 29, 30, 31, 32, 33, 34, 35};
    private final int[] phasenAuswahlGrayGlasPane = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 22, 23, 24, 25, 26, 28, 29, 30, 31, 32, 33, 34, 35};
    private final int[] Befehle3 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 20, 23, 24, 25, 26, 28, 29, 30, 31, 32, 33, 34, 35};
    private final int[] Biom = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35};
    private final Map<UUID, Long> zombieCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 4000; // Millisekunden

    private final int MAX_CLICKS = 3;

    public static Inventory upgradeShop;
    public static Inventory mainGUI;
    public static Inventory Einstellungen;
    public static Inventory Rebirth;
    public static Inventory Befehle;
    public static Inventory Auswahl;
    public static Inventory Verwaltung;
    public static Inventory switchBiomeGUI;

    private final HashMap<UUID, Integer> deleteClicks = new HashMap<>();
    private final HashMap<UUID, Integer> rebirthClicks = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        if (mainGUI == null || Auswahl == null || Einstellungen == null || Rebirth == null || Befehle == null || Verwaltung == null || switchBiomeGUI == null) {

        }
        ItemStack grayglas = new ItemStack(GRAY_STAINED_GLASS_PANE);
        ItemMeta graygalsmetea = grayglas.getItemMeta();
        graygalsmetea.setHideTooltip(true);
        graygalsmetea.setCustomModelData(0);
        grayglas.setItemMeta(graygalsmetea);
        createguis(player, grayglas);
        createBiomGUI(grayglas);

        deleteClicks.put(player.getUniqueId(), MAX_CLICKS);
        rebirthClicks.put(player.getUniqueId(), MAX_CLICKS);

        updateVerwaltungGUI(player);
        player.openInventory(mainGUI);
        return true;
    }

    private void createguis(Player player, ItemStack grayglas) {

        Einstellungen = Bukkit.createInventory(null, 27, "§cInsel-Einstellungen");
        Rebirth = Bukkit.createInventory(null, 27, "§eRebirth");
        Befehle = Bukkit.createInventory(null, 36, "§8Spielerbefehle");
        Auswahl = Bukkit.createInventory(null, 36, "§aPhasen-Auswahl");
        Verwaltung = Bukkit.createInventory(null, 36, "§cInsel-Verwaltung");
        mainGUI = Bukkit.createInventory(null, 9, "§aOneBlock-Menü");


        for (int pos : grayglasmaingui) {
            mainGUI.setItem(pos, grayglas);
        }

        for (int pos : verwaltungGrayGlasPane) {
            Verwaltung.setItem(pos, grayglas);
        }

        for (int pos : phasenAuswahlGrayGlasPane) {
            Auswahl.setItem(pos, grayglas);

        }



        for (int pos : Befehle3) {
            Befehle.setItem(pos, grayglas);
        }

        setPlayerHeadInMainGUI(player);


        ItemStack xpBottle = new ItemStack(EXPERIENCE_BOTTLE);
        ItemMeta meta6 = xpBottle.getItemMeta();
        if (meta6 != null) {
            meta6.setDisplayName("§aPhasen-Auswahl");
            List<String> lore = new ArrayList<>();

            lore.add(" ");
            lore.add("§fÄndere deine Phase!");
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
            lore.add("§fVerwalte deine Insel");
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

        ItemStack befehl10 = new ItemStack(SHORT_GRASS);
        ItemMeta befehlmeta10 = befehl10.getItemMeta();
        if (befehlmeta10 != null) {
            befehlmeta10.setDisplayName("§a/ob switchBiome ");
            befehl10.setItemMeta(befehlmeta10);
        }

        Befehle.setItem(21, befehl10);
        ItemStack befehl11 = new ItemStack(TALL_GRASS);
        ItemMeta befehlmeta11 = befehl9.getItemMeta();
        if (befehlmeta11 != null) {
            befehlmeta11.setDisplayName("§a/ob switchIslandBiome");
            befehl11.setItemMeta(befehlmeta11);
        }
        Befehle.setItem(22, befehl11);

        ItemStack zurück1 = new ItemStack(RED_DYE);
        ItemMeta zurückmeta1 = zurück1.getItemMeta();
        if (zurückmeta1 != null) {
            zurückmeta1.setDisplayName("§cZurück zum §aOneBlock-Menü");
            zurück1.setItemMeta(zurückmeta1);
        }
        Befehle.setItem(27, zurück1);


    }
    private void createBiomGUI(ItemStack grayglas){
        switchBiomeGUI = Bukkit.createInventory(null, 36, "§aBiom Verwaltung");
        for (int pos : Biom) {
            if(switchBiomeGUI == null){
                createBiomGUI(grayglas);
            }
            switchBiomeGUI.setItem(pos, grayglas);

        }
        ItemStack zurück1 = new ItemStack(RED_DYE);
        ItemMeta zurückmeta1 = zurück1.getItemMeta();
        if (zurückmeta1 != null) {
            zurückmeta1.setDisplayName("§cZurück zum §aOneBlock-Menü");
            zurück1.setItemMeta(zurückmeta1);
        }
        switchBiomeGUI.setItem(27, zurück1);
    }

    private void setPlayerHeadInMainGUI(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack skull = new ItemStack(PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName("§eBefehle");
            List<String> lore = new ArrayList<>();

            lore.add(" ");
            lore.add("§fDein Profil:");
            lore.add(" ");

            boolean hatInsel = DBM.getBoolean("userdata", uuid, "EigeneInsel", false);

            if (hatInsel) {

                lore.add("§cDein OneBlock Level: " + "§b" + DBM.getInt("userdata", uuid, "IslandLevel", 1));


                int x = DBM.getInt("userdata", uuid, "OneBlock_x", 0);
                int z = DBM.getInt("userdata", uuid, "OneBlock_z", 0);

                int Missing = DBM.getInt("userdata", uuid, "MissingBlocksToLevelUp", 1);
                int total = DBM.getInt("userdata", uuid, "MissingBlocksToLevelUp", 1);

                lore.add("§cDein OneBlock Standort: " + "§b" + "X: " + x + ", Z: " + z);
                lore.add("§cVerbleibende Blöcke bis zum Level up: " + "§b" + Missing + " | " + total);
                lore.add("§cDeine Worldborder größe: " + "§b" + +DBM.getInt("userdata", uuid, "WorldBorderSize", 1));
            } else {
                lore.add("§bDu besitzt §ckeine Insel!");

            }

            skullMeta.setLore(lore);
            skull.setItemMeta(skullMeta);


        }
        mainGUI.setItem(4, skull);
    }


    private void updateVerwaltungGUI(Player player) {
        UUID uuid = player.getUniqueId();
        int deleteRemaining = deleteClicks.getOrDefault(uuid, MAX_CLICKS);
        int rebirthRemaining = rebirthClicks.getOrDefault(uuid, MAX_CLICKS);

        for (int slot : new int[]{10, 12, 14, 16, 20, 22}) {
            ItemStack item;
            String displayName;
            List<String> lore = new ArrayList<>();

            switch (slot) {
                case 10 -> {
                    item = new ItemStack(BARRIER);
                    displayName = "§cInsel-Löschen";
                    lore.add(" ");
                    lore.add("§cInsel Löschung nicht mehr rückgängig!");
                    lore.add(" ");
                    lore.add("§7Klicke §e" + deleteRemaining + " §7Mal zum §cLöschen§7!");
                }
                case 12 -> {
                    item = new ItemStack(TOTEM_OF_UNDYING);
                    displayName = "§cRebirth";
                    lore.add(" ");
                    lore.add("§bDeine OneBlock wird wieder auf §4Level 1 §bgesetzt!");
                    lore.add("§bAber du bekommst Belohnungen für den §cRebirth");
                    lore.add(" ");
                    lore.add("§7Klicke §e" + rebirthRemaining + " §7Mal zum §cRebirth§7!");
                }
                case 14 -> {
                    item = new ItemStack(STRUCTURE_VOID);
                    displayName = "§aWorldBorder Größe";
                    int currentSize = DBM.getInt("userdata", uuid, "WorldBorderSize", 1);

                    int basePrice = 100;
                    int upgradesDone = (currentSize - 50) / 10;
                    int price = (int) (basePrice * Math.pow(2, upgradesDone));

                    lore.add("§7Aktuelle Größe: §e" + currentSize);
                    lore.add("§7Klicke, um die Größe zu erhöhen");
                    lore.add("§7(Maximal 200)");
                    lore.add("§7Preis für nächstes Upgrade: §e" + price + " Coins");
                }
                case 16 -> {
                    item = new ItemStack(TALL_GRASS);
                    displayName = "§aInsel Biom ändern";
                    String currentBiome = DBM.getString("userdata", uuid, "IslandBiom", "PLAINS");
                    lore.add("");
                    lore.add("§7Aktuelles Biom : §e" + currentBiome);
                }
                case 20 -> {
                    item = new ItemStack(ZOMBIE_HEAD);
                    displayName = "§aOneblock Mob Spawning";
                    Boolean currentValue = Boolean.valueOf(String.valueOf(DBM.getBoolean("userdata", uuid, "MobSpawning", false)));

                    while (lore.size() <= 1) {
                        lore.add("");
                    }
                        lore.set(1, "Wert");
                        if (currentValue == true) {
                            lore.set(1, "§bAktuell: §c" + currentValue);
                        } else if (currentValue == false){
                            lore.set(1, "§bAktuell: §c" + currentValue);
                        }
                    }
                case 2 -> {
                    item = new ItemStack(ZOMBIE_HEAD);
                    displayName = "§aOn2eblock Mob Spawning";
                    Boolean currentValue = Boolean.valueOf(String.valueOf(DBM.getBoolean("userdata", uuid, "MobSpawning", false)));

                    while (lore.size() <= 1) {
                        lore.add(""); // Platzhalter hinzufügen
                    }
                    lore.set(1, "Wert");
                    if (currentValue == true) {
                        lore.set(1, "§bAktuell: §c" + currentValue);
                    } else if (currentValue == false) {
                        lore.set(1, "§bAktuell: §c" + currentValue);
                    }
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
            Verwaltung.setItem(27, zurück);
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
        if (event.getClickedInventory() == null) return;

        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        String inventoryTitle = player.getOpenInventory().getTitle();

        // Liste der blockierten Inventartitel
        List<String> blockedInventories = Arrays.asList(
                "§cInsel-Einstellungen",
                "§eRebirth",
                "§8Spielerbefehle",
                "§aPhasen-Auswahl",
                "§cInsel-Verwaltung",
                "§aOneBlock-Menü"
        );



        if (blockedInventories.contains(inventoryTitle)) {
            // Interaktionen im Top-Inventar komplett verhindern
            if (clickedInventory.equals(topInventory)) {
                event.setCancelled(true);
            }

            // Verschieben von unten nach oben verhindern (Shift-Klick etc.)
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                // Prüfe, ob der Zielinventar das Top-Inventar ist
                Inventory destinationInventory = event.getView().getTopInventory();

                // Nur blockieren, wenn Ziel das Top-Inventar ist
                if (destinationInventory.equals(topInventory)) {
                    event.setCancelled(true);
                }
            }
        }



        UUID uuid = player.getUniqueId();

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();
        assert clicked != null;
        Material type = clicked.getType();


        if (title.equalsIgnoreCase("§cInsel-Verwaltung")) {


            switch (type) {

                case STRUCTURE_VOID -> {
                    addWorldBoarder(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

                }
                case TALL_GRASS -> {
                    player.openInventory(switchBiomeGUI);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

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
                case ZOMBIE_HEAD -> {
                    UUID playerUUID = player.getUniqueId();
                    long currentTime = System.currentTimeMillis();


                    // Überprüfen, ob der Spieler noch in der Abklingzeit ist
                    if (zombieCooldowns.containsKey(playerUUID) && zombieCooldowns.get(playerUUID) > currentTime) {
                        long remainingTime = (zombieCooldowns.get(playerUUID) - currentTime) / 1000;
                        player.sendMessage(Main.getPrefix() + " §cBitte warte " + remainingTime + " Sekunden, bevor du erneut klickst!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f); // Signalton
                        return;
                    }
                    zombieCooldowns.put(playerUUID, currentTime + COOLDOWN_TIME);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    boolean currentValue = DBM.getBoolean("userdata", playerUUID, "MobSpawning", true);
                    boolean newValue = !currentValue;
                    DBM.setBoolean("userdata", playerUUID, "MobSpawning", newValue);
                    OneBlockManager.savePlayerData(uuid);
                    updateVerwaltungGUI(player);
                }

                case RED_DYE -> {
                    player.openInventory(mainGUI);
                }
                default -> {

                }
            }
        } else if (title.equalsIgnoreCase("§aOneBlock-Menü")) {
            switch (type) {
                case EXPERIENCE_BOTTLE -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    player.openInventory(Auswahl);
                }
                case PLAYER_HEAD -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    player.openInventory(Befehle);
                }
                case COMPARATOR -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    deleteClicks.put(uuid, MAX_CLICKS);
                    rebirthClicks.put(uuid, MAX_CLICKS);
                    updateVerwaltungGUI(player);
                    player.openInventory(Verwaltung);
                }
                default -> {

                }
            }
        }
        else if (title.equalsIgnoreCase("§aPhasen-Auswahl")) {



            boolean durchgespielt = DBM.getBoolean("userdata", uuid, "Durchgespielt", false);
            switch (type) {
                case GRASS_BLOCK:

                    if (durchgespielt) {
                        DBM.setInt("userdata", uuid, "IslandLevel", 1);
                        DBM.setInt("userdata", uuid, "MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.1.blockcount"));
                        DBM.setInt("userdata", uuid, "TotalBlocks", Main.config.getInt("oneblockblocks.1.blockcount"));
                        DBM.setBoolean("userdata", uuid, "Durchgespielt", true);
                        player.sendMessage(Main.getPrefix() + "§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 1 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage("§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case OAK_LOG:

                    if (durchgespielt) {
                        DBM.setInt("userdata", uuid, "IslandLevel", 2);
                        DBM.setInt("userdata", uuid, "MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.2.blockcount"));
                        DBM.setInt("userdata", uuid, "TotalBlocks", Main.config.getInt("oneblockblocks.2.blockcount"));
                        DBM.setBoolean("userdata", uuid, "Durchgespielt", true);

                        player.sendMessage(Main.getPrefix() + "§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 2 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage(Main.getPrefix() + "§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case STONE:

                    if (durchgespielt) {
                        DBM.setInt("userdata", uuid, "IslandLevel", 3);
                        DBM.setInt("userdata", uuid, "MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.3.blockcount"));
                        DBM.setInt("userdata", uuid, "TotalBlocks", Main.config.getInt("oneblockblocks.3.blockcount"));
                        DBM.setBoolean("userdata", uuid, "Durchgespielt", true);
                        player.sendMessage(Main.getPrefix() + "§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 3 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage(Main.getPrefix() + "§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case IRON_ORE:

                    if (durchgespielt) {
                        DBM.setInt("userdata", uuid, "IslandLevel", 4);
                        DBM.setInt("userdata", uuid, "MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.4.blockcount"));
                        DBM.setInt("userdata", uuid, "TotalBlocks", Main.config.getInt("oneblockblocks.4.blockcount"));
                        DBM.setBoolean("userdata", uuid, "Durchgespielt", true);
                        player.sendMessage(Main.getPrefix() + "§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 4 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage(Main.getPrefix() + "§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case DIAMOND_BLOCK:

                    if (durchgespielt) {
                        DBM.setInt("userdata", uuid, "IslandLevel", 5);
                        DBM.setInt("userdata", uuid, "MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.5.blockcount"));
                        DBM.setInt("userdata", uuid, "TotalBlocks", Main.config.getInt("oneblockblocks.5.blockcount"));
                        DBM.setBoolean("userdata", uuid, "Durchgespielt", true);

                        player.sendMessage(Main.getPrefix() + "§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 5 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage(Main.getPrefix() + "§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case NETHERRACK:

                    if (durchgespielt) {
                        DBM.setInt("userdata", uuid, "IslandLevel", 6);
                        DBM.setInt("userdata", uuid, "MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.6blockcount"));
                        DBM.setInt("userdata", uuid, "TotalBlocks", Main.config.getInt("oneblockblocks.6.blockcount"));
                        DBM.setBoolean("userdata", uuid, "Durchgespielt", true);

                        player.sendMessage(Main.getPrefix() + "§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 6 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage(Main.getPrefix() + "§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case WARPED_STEM:

                    if (durchgespielt) {
                        DBM.setInt("userdata", uuid, "IslandLevel", 7);
                        DBM.setInt("userdata", uuid, "MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.7.blockcount"));
                        DBM.setInt("userdata", uuid, "TotalBlocks", Main.config.getInt("oneblockblocks.7.blockcount"));
                        DBM.setBoolean("userdata", uuid, "Durchgespielt", true);
                        player.sendMessage(Main.getPrefix() + "§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 7 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage(Main.getPrefix() + "§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case END_STONE:

                    if (durchgespielt) {
                        DBM.setInt("userdata", uuid, "IslandLevel", 8);
                        DBM.setInt("userdata", uuid, "MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.8.blockcount"));
                        DBM.setInt("userdata", uuid, "TotalBlocks", Main.config.getInt("oneblockblocks.8.blockcount"));
                        DBM.setBoolean("userdata", uuid, "Durchgespielt", true);

                        player.sendMessage(Main.getPrefix() + "§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 8 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage(Main.getPrefix() + "§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case NETHERITE_BLOCK:

                    if (durchgespielt) {
                        DBM.setInt("userdata", uuid, "IslandLevel", 9);
                        DBM.setInt("userdata", uuid, "MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.9.blockcount"));
                        DBM.setInt("userdata", uuid, "TotalBlocks", Main.config.getInt("oneblockblocks.9.blockcount"));
                        DBM.setBoolean("userdata", uuid, "Durchgespielt", true);

                        player.sendMessage(Main.getPrefix() + "§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 9 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage(Main.getPrefix() + "§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case BEACON:

                    if (durchgespielt) {
                        DBM.setInt("userdata", uuid, "IslandLevel", 10);
                        DBM.setInt("userdata", uuid, "MissingBlocksToLevelUp", Main.config.getInt("oneblockblocks.10.blockcount"));
                        DBM.setInt("userdata", uuid, "TotalBlocks", Main.config.getInt("oneblockblocks.10.blockcount"));
                        DBM.setBoolean("userdata", uuid, "Durchgespielt", true);

                        player.sendMessage(Main.getPrefix() + "§aDeine OneBlock-Phase wurde erfolgreich auf " + "§c" + Main.config.getString("oneblockblocks." + 10 + ".name", "Unbekannt") + "§a" + " zurückgesetzt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        player.closeInventory();
                    } else {
                        player.sendMessage(Main.getPrefix() + "§cDu musst OneBlock einmal komplett durchgespielt haben, um diese Phase auszuwählen.");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                    }
                    break;

                case RED_DYE:
                    player.openInventory(mainGUI);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    break;
            }
        } else if (title.equalsIgnoreCase("§8Spielerbefehle")) {


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

                case SHORT_GRASS:
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    sendSuggestCommandMessage(player, "switchBiome ");
                    break;
                case TALL_GRASS:
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    sendSuggestCommandMessage(player, "switchIslandBiome ");
                    break;


                case RED_DYE:
                    player.openInventory(mainGUI);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    break;

                default:
                    break;
            }

        } else if (title.equalsIgnoreCase("§aBiom Verwaltung")) {

            switch (type) {
                case GRASS_BLOCK:
                    player.performCommand("ob switchIslandBiome PLAINS");
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    player.closeInventory();
                    break;



                case RED_DYE:
                    player.openInventory(mainGUI);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    break;

                default:
                    break;
            }
        }
    }

    private void sendSuggestCommandMessage(Player player, String command) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

        TextComponent msg = new TextComponent(Main.getPrefix() + "§aKlicke hier, um den Befehl einzugeben: ");
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

    public static void addWorldBoarder(Player player) {

        UUID uuid = player.getUniqueId();

        int currentSize = DBM.getInt("userdata", uuid, "WorldBorderSize", 1);
        int maxSize = 200;

        if (currentSize >= maxSize) {
            player.sendMessage(Main.getPrefix() + "§cDu hast das Limit erreicht");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
            return;
        }

        int basePrice = 20000;
        int upgradesDone = (currentSize - 50) / 10;
        int price = (int) (basePrice * Math.pow(2, upgradesDone));

        if (eco.getBalance(player) < price) {
            player.sendMessage(Main.getPrefix() + "§cDu hast nicht genug Geld! Benötigt: §e" + price + " Coins");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }


        eco.withdrawPlayer(player, price);


        currentSize += 10;
        DBM.setInt("userdata", uuid, "WorldBorderSize", currentSize);

        player.sendMessage(Main.getPrefix() + "§aDeine WorldBorder wurde auf §e" + currentSize + " §avergrößert!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        player.closeInventory();


    }

}