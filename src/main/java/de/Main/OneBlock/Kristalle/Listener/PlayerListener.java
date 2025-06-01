package de.Main.OneBlock.Kristalle.Listener;

import de.Main.OneBlock.Kristalle.GUI.UpgradeGUI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
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
    private final Map<UUID, Long> clickCooldowns = new HashMap<>();
    private static final long CLICK_COOLDOWN_MS = 1000;

    public PlayerListener(JavaPlugin plugin, GrowthManager growthManager, FileConfiguration growthConfig, File growthFile, Economy economy) {
        this.plugin = plugin;
        this.growthManager = growthManager;
        this.economy = economy;
        this.upgradeGUI = new UpgradeGUI(growthManager, economy);

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
            growthManager.saveGrowth(loc, SMALL_AMETHYST_BUD, now + GrowthManager.getGrowthTimeSeconds(SMALL_AMETHYST_BUD) * 1000L, player.getUniqueId(), level, 0);

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
                    growthManager.saveGrowth(loc, next, nextGrowth, owner, growthManager.getLevel(loc), 0);

                    if (next != AMETHYST_CLUSTER) {
                        startGrowthCycle(block, next, owner);
                    }
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
                    return;
                }
            }
            e.setCancelled(true);
            player.sendMessage("§cDu kannst Kristall-Blöcke nur mit einem Item mit Treue (Loyalty) Stufe 5 oder höher platzieren!");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;

        Player player = e.getPlayer();
        Block block = e.getClickedBlock();

        if (block.getType() == AMETHYST_CLUSTER) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
                e.setCancelled(true);
                openUpgradeGUIs.put(player.getUniqueId(), block.getLocation());
                upgradeGUI.open(player, block.getLocation());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        if (!openUpgradeGUIs.containsKey(player.getUniqueId())) return;
        if (!e.getView().getTitle().equals("§bUpgrade-Menü")) return;

        InventoryAction action = e.getAction();
        if (action != InventoryAction.PICKUP_ONE && action != InventoryAction.PICKUP_ALL) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

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

        if (clicked.getType() == TOTEM_OF_UNDYING) {
            // Dein Upgrade-Code bleibt wie er ist
            // ...
        } else if (clicked.getType() == BARRIER) {
            // *** Neuer Code für Abbau ***

            // Prüfe Item in der Hand für Treue 5
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand == null || !itemInHand.containsEnchantment(Enchantment.LOYALTY) || itemInHand.getEnchantmentLevel(Enchantment.LOYALTY) < 5) {
                player.sendMessage("§cDu brauchst ein Item mit Treue (Loyalty) Stufe 5, um den Kristall abzubauen!");
                return;
            }

            // Block abbauen (ersetze durch AIR)
            Block block = blockLoc.getBlock();
            if (block.getType() != Material.AMETHYST_CLUSTER) {
                player.sendMessage("§cAn dieser Stelle befindet sich kein Kristall!");
                return;
            }

            int level = growthManager.getLevel(blockLoc);

            block.setType(Material.AIR);

            // Level speichern (ist eigentlich schon in GrowthManager, aber sicherheitshalber nochmal)
            growthManager.setLevel(blockLoc, level);

            // Config-Eintrag löschen (damit der Kristall nicht weiter wächst)
            growthManager.removeGrowth(blockLoc);

            player.sendMessage("§aKristall abgebaut! Dein Level wurde gespeichert.");

            // GUI schließen oder neu öffnen (optional)
            player.closeInventory();
        }
    }
}
