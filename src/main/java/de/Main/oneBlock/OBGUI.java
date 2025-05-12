package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class OBGUI implements CommandExecutor {

    // Speichert das GUI, damit es nicht mehrfach erstellt wird
    public Inventory mainGUI;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;

        if (mainGUI == null) {
            mainGUI = Bukkit.createInventory(null, 6 * 9, "OneBlock");
        }

        player.openInventory(mainGUI);
        return true;
    }
}
