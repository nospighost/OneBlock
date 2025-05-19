package de.Main.OneBlock;

import org.bukkit.Bukkit;

import org.bukkit.Material;

import org.bukkit.block.Block;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;


public class OBItems implements CommandExecutor, Listener {
    private Inventory globalTrashInventory;
    private final JavaPlugin plugin;
    private Inventory onechest;



    public OBItems(JavaPlugin plugin) {
        this.plugin = plugin;
        globalTrashInventory = Bukkit.createInventory(null, 54, "§6Globaler Mülleimer");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können das nutzen!");
            return true;
        }
        Player player = (Player) sender;
        player.openInventory(globalTrashInventory);
        return true;
    }


    public void start() {
        Bukkit.getScheduler().runTaskTimer(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("oneblockplugin")), () -> {
            globalTrashInventory.clear();
        }, 0L, 5 * 60 * 20L);
    }


    @EventHandler(ignoreCancelled = true)
    public void onShiftRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if(clicked.getType() == Material.PODZOL) {
            player.openInventory(globalTrashInventory);

        }

    }

}








