package de.Main.OneBlock.Oneblock.GUI.Listener;


import de.Main.OneBlock.Main;
import de.Main.OneBlock.Oneblock.GUI.OneBlockGUI.OBGUI;
import de.Main.OneBlock.Oneblock.Manager.Manager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static de.Main.OneBlock.Oneblock.Manager.Manager.getIslandConfig;
import static org.bukkit.Material.BARRIER;
import static org.bukkit.Material.TOTEM_OF_UNDYING;

public class InventoryClickManager implements Listener {
    public static final int MAX_CLICKS = 3;
    public static final HashMap<UUID, Integer> deleteClicks = new HashMap<>();
    public static final HashMap<UUID, Integer> rebirthClicks = new HashMap<>();

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
                    OBGUI.addWorldBoarder(config, player);
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
                    player.openInventory(OBGUI.mainGUI);
                }
                default -> {

                }
            }
        }

        if (title.equalsIgnoreCase("§aOneBlock-Menü")) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

            switch (type) {
                case EXPERIENCE_BOTTLE -> player.openInventory(OBGUI.Auswahl);
                case PLAYER_HEAD -> player.openInventory(OBGUI.Befehle);
                case COMPARATOR -> {
                    deleteClicks.put(uuid, MAX_CLICKS);
                    rebirthClicks.put(uuid, MAX_CLICKS);
                    OBGUI.updateVerwaltungGUI(player);
                    player.openInventory(OBGUI.Verwaltung);
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
                    player.openInventory(OBGUI.mainGUI);
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
                    player.openInventory(OBGUI.mainGUI);
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

}
