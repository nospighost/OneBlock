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

import java.util.ArrayList;
import java.util.List;

public class OBGUI implements CommandExecutor, Listener {

    public static Inventory mainGUI;
    public static Inventory upgradeShop;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;

        createMainGUI();
        player.openInventory(mainGUI);
        return true;
    }

    private void createMainGUI() {
        if (mainGUI != null) return;

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
        ItemMeta grassMeta = grass.getItemMeta();
        if (grassMeta != null) {
            grassMeta.setDisplayName("§aZu deinem OneBlock teleportieren");
            grass.setItemMeta(grassMeta);
        }
        mainGUI.setItem(20, grass);

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.setDisplayName("§cDeinen OneBlock löschen");
            barrier.setItemMeta(barrierMeta);
        }
        mainGUI.setItem(22, barrier);

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

    private void openUpgradeShop(Player player) {
        upgradeShop = Bukkit.createInventory(null, 6 * 9, "Upgrade-Shop");

        YamlConfiguration config = Manager.getIslandConfig(player);
        int currentSize = config.getInt("WorldBorderSize", 50);
        int costLevel = ((currentSize - 40) / 10) + 1;
        int neededLevel = costLevel * 2;
        int playerLevel = config.getInt("IslandLevel", 1);

        ItemStack upgradeItem = new ItemStack(Material.STRUCTURE_VOID);
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

        ItemStack rebirth = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta rebirthMeta = rebirth.getItemMeta();
        if (rebirthMeta != null) {
            rebirthMeta.setDisplayName(" ");
            rebirth.setItemMeta(rebirthMeta);
            upgradeShop.setItem(22, rebirth);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();
        Material type = clicked.getType();

        if (title.equalsIgnoreCase("OneBlock")) {
            event.setCancelled(true);

            switch (type) {
                case GRASS_BLOCK:
                    player.closeInventory();
                    player.performCommand("ob join");
                    break;
                case BARRIER:
                    player.closeInventory();
                    player.performCommand("ob delete");
                    break;
                case BEACON:
                    player.closeInventory();
                    openUpgradeShop(player);
                    break;
                case ENDER_PEARL:
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    TextComponent msg = new TextComponent("§aKlicke hier, um den Besuchsbefehl einzugeben: ");
                    TextComponent commandPart = new TextComponent("§e/ob visit ");
                    commandPart.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ob visit "));
                    msg.addExtra(commandPart);
                    player.spigot().sendMessage(msg);
                    break;
            }
        }

        if (title.equalsIgnoreCase("Upgrade-Shop")) {
            event.setCancelled(true);



            if (type == Material.STRUCTURE_VOID) {
                YamlConfiguration config = Manager.getIslandConfig(player);
                int currentSize = config.getInt("WorldBorderSize", 50);

                if(!(currentSize >= 200)){


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
                }else {
                    player.sendMessage("§cDu hast das Limit erreicht");
                }


                }

        }
    }
}
