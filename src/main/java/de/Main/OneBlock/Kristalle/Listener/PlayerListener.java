package de.Main.OneBlock.Kristalle.Listener;

import de.Main.OneBlock.Kristalle.GUI.UpgradeGUI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Material.*;

public class PlayerListener implements Listener {

    private final JavaPlugin plugin;
    private final GrowthManager growthManager;
    private final Economy economy;
    private final UpgradeGUI upgradeGUI;

    private final Map<UUID, Location> openUpgradeGUIs = new HashMap<>();

    public PlayerListener(JavaPlugin plugin, GrowthManager growthManager, Economy economy) {
        this.plugin = plugin;
        this.growthManager = growthManager;
        this.economy = economy;
        this.upgradeGUI = new UpgradeGUI(growthManager, economy);

        // Scheduler: alle 5 Minuten 5000$ an Spieler mit Level 10+ Kristall zahlen
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (hasLevel10Crystal(player.getUniqueId())) {
                    economy.depositPlayer(player, 5000);
                    player.sendMessage("§aDu hast 5000 Dollar für deinen Level 10 Kristall erhalten!");
                }
            }
        }, 0L, 20L * 60 * 5);
    }

    private boolean hasLevel10Crystal(UUID playerUUID) {
        if (!growthManager.growthConfig.isConfigurationSection("growth")) return false;
        for (String world : growthManager.growthConfig.getConfigurationSection("growth").getKeys(false)) {
            for (String coord : growthManager.growthConfig.getConfigurationSection("growth." + world).getKeys(false)) {
                String owner = growthManager.growthConfig.getString("growth." + world + "." + coord + ".owner");
                int level = growthManager.growthConfig.getInt("growth." + world + "." + coord + ".Level");
                if (owner != null && owner.equals(playerUUID.toString()) && level >= 10) return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (block.getType() == SMALL_AMETHYST_BUD || block.getType() == MEDIUM_AMETHYST_BUD || block.getType() == LARGE_AMETHYST_BUD) {
            e.setCancelled(true);
            return;
        }

        if (block.getType() == AMETHYST_CLUSTER) {
            e.setCancelled(true);
            block.setType(SMALL_AMETHYST_BUD);

            Location loc = block.getLocation();
            int level = growthManager.getLevel(loc);
            int payout = 1 + level;
            economy.depositPlayer(player, payout);
            player.sendMessage("§a+§e" + payout + "$ verdient!");

            long now = System.currentTimeMillis();
            growthManager.saveGrowth(loc, SMALL_AMETHYST_BUD, now + GrowthManager.getGrowthTimeSeconds(SMALL_AMETHYST_BUD) * 1000L, player.getUniqueId(), level, growthManager.getPrestige(loc));

            // Starte Wachstum in Background
            startGrowthCycle(block, SMALL_AMETHYST_BUD, player.getUniqueId());
        }
    }

    private void startGrowthCycle(Block block, Material stage, UUID owner) {
        Material next = GrowthManager.getNextStage(stage);
        if (next == null) return;

        int delay = GrowthManager.getGrowthTimeSeconds(stage);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (block.getType() == stage) {
                    block.setType(next);
                    Location loc = block.getLocation();
                    long nextGrowth = (next == AMETHYST_CLUSTER) ? 0 : System.currentTimeMillis() + GrowthManager.getGrowthTimeSeconds(next) * 1000L;
                    growthManager.saveGrowth(loc, next, nextGrowth, owner, growthManager.getLevel(loc), growthManager.getPrestige(loc));

                    // rekursiv falls noch nicht Endstadium
                    if(next != AMETHYST_CLUSTER) startGrowthCycle(block, next, owner);
                }
            }
        }.runTaskLater(plugin, delay * 20L);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (block.getType() == AMETHYST_CLUSTER) {
            if (itemInHand != null && itemInHand.containsEnchantment(org.bukkit.enchantments.Enchantment.LOYALTY)) {
                int loyaltyLevel = itemInHand.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.LOYALTY);
                if (loyaltyLevel >= 5) {
                    // Platzieren erlaubt
                    return;
                }
            }
            // Platzieren verhindern und Spieler informieren
            e.setCancelled(true);
            player.sendMessage("§cDu kannst Kristall-Blöcke nur mit einem Item mit Treue (Loyalty) Stufe 5 oder höher platzieren!");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;

        Player player = e.getPlayer();
        Block block = e.getClickedBlock();

        // Prüfen ob es ein Amethyst Cluster ist
        if (block.getType() == Material.AMETHYST_CLUSTER) {
            // Nur bei Rechtsklick auf Block und wenn Shift gedrückt ist
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
                e.setCancelled(true);
                openUpgradeGUIs.put(player.getUniqueId(), block.getLocation());
                upgradeGUI.open(player, block.getLocation());
            }
        }
    }
    public void removePrestigeItem(Player player) {
        // Beispiel: Inventory holen und Item entfernen
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (inv == null) return;

        // Suche das Prestige-Item (EXP Bottle) und entferne es
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == Material.EXPERIENCE_BOTTLE) {
                inv.setItem(i, null);
                break;
            }
        }
    }


    private final Map<UUID, Long> clickCooldowns = new HashMap<>();
    private static final long CLICK_COOLDOWN_MS = 1000;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        if (!openUpgradeGUIs.containsKey(player.getUniqueId())) return;
        if (!e.getView().getTitle().equals("§bUpgrade-Menü")) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        // Cooldown check
        long now = System.currentTimeMillis();
        if (clickCooldowns.containsKey(player.getUniqueId())) {
            long lastClick = clickCooldowns.get(player.getUniqueId());
            if (now - lastClick < CLICK_COOLDOWN_MS) {
                player.sendMessage("§cBitte nicht so schnell klicken!");
                return;
            }
        }
        clickCooldowns.put(player.getUniqueId(), now);

        Location blockLoc = openUpgradeGUIs.get(player.getUniqueId());

        if (clicked.getType() == Material.TOTEM_OF_UNDYING) {
            // Upgrade
            int level = growthManager.getLevel(blockLoc);
            if (level >= 20) {
                player.sendMessage("§cMaximales Level erreicht!");
                return;
            }

            int price = upgradeGUI.getUpgradePrice(level);
            if (economy.getBalance(player) < price) {
                player.sendMessage("§cDu hast nicht genug Geld!");
                return;
            }

            economy.withdrawPlayer(player, price);
            growthManager.setLevel(blockLoc, level + 1);
            player.sendMessage("§aKristall auf Level " + (level + 1) + " geupgradet!");
            upgradeGUI.open(player, blockLoc);

        } else if (clicked.getType() == Material.EXPERIENCE_BOTTLE) {
            // Prestige
            int prestige = growthManager.getPrestige(blockLoc);
            int level = growthManager.getLevel(blockLoc);

            // Voraussetzungen fürs Prestigen
            if (prestige >= 10) {
                player.sendMessage("§cMaximaler Prestige erreicht!");
                return;
            }
            if (level < 20) {
                player.sendMessage("§cDu brauchst Level 20, um zu prestigen!");
                return;
            }

            int prestigeCost = 10000; // Beispielkosten fürs Prestigen
            if (economy.getBalance(player) < prestigeCost) {
                player.sendMessage("§cDu hast nicht genug Geld zum Prestigen! (" + prestigeCost + "$)");
                return;
            }

            // Geld abziehen fürs Prestigen
            economy.withdrawPlayer(player, prestigeCost);

            // Prestigen ausführen
            growthManager.setPrestige(blockLoc, prestige + 1);
            growthManager.setLevel(blockLoc, 0);

            // Belohnung fürs Prestigen, z.B. 5000$
            economy.depositPlayer(player, 5000);

            player.sendMessage("§aDu hast geprestigt! Prestige: " + (prestige + 1));
            upgradeGUI.open(player, blockLoc);

            // Item entfernen bei max Prestige (10)
            if (prestige + 1 >= 10) {
                // Entferne den Prestige-Button aus GUI
                removePrestigeItem(player);
                player.sendMessage("§6Prestige-Item wurde entfernt, Max Prestige erreicht!");
            }
        }
    }

}
