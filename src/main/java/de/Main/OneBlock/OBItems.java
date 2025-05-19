package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class OBItems implements CommandExecutor, Listener {
    private Inventory globalTrashInventory;

    private Inventory onechest;

    public OBItems() {
        globalTrashInventory = Bukkit.createInventory(null, 54, "§6Globaler Mülleimer");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können das nutzen!");
            return true;
        }
        Player player = (Player) sender;
        player.openInventory(globalTrashInventory); // Gleiches Inventory für alle
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.CHEST) return;

        Chest chest = (Chest) clicked.getState();


        //if (chest.getCustomName() != null && chest.getCustomName().equals("§6Erste Kiste")) {
        //    event.setCancelled(true); // Standard-Öffnen abbrechen
//

            if (onechest == null) {
                onechest = Bukkit.createInventory(null, 27, "§6Ibecgest");
                onechest.setItem(3, new ItemStack(Material.STONE));
                onechest.setItem(13, new ItemStack(Material.DIAMOND));
                onechest.setItem(23, new ItemStack(Material.DIRT));
                onechest.setItem(21, new ItemStack(Material.COBBLESTONE));
            }


            player.openInventory(onechest);
        }
    }








