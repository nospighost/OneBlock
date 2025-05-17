package de.Main.OneBlock;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static de.Main.OneBlock.Main.*;

public class Manager implements Listener {
    public static Economy economy;
    private final JavaPlugin plugin;
    static String prefix = Main.config.getString("Server");


    public Manager(Economy eco, JavaPlugin plugin) {
        economy = eco;
        this.plugin = plugin;
    }

    // Insel erstellen oder joinen wenn halt keine da ist
    public static boolean createOrJoinIsland(Player player, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("join")) {

            YamlConfiguration config = Manager.getIslandConfig(player);
            if (!config.contains("IslandSpawn-x") || !config.getBoolean("EigeneInsel") ||
                    !config.contains("IslandSpawn-z") || !config.contains("OneBlock-x") || !config.contains("OneBlock-z")) {

                int padding = config.getInt("value");
                int pos = getIslandCords(padding);

                config.set("OneBlock-x", pos);
                config.set("OneBlock-z", pos);
                config.set("x-position", pos);
                config.set("z-position", pos);
                config.set("IslandSpawn-x", pos);
                config.set("IslandSpawn-z", pos);
                config.set("WorldBorderSize", 50);
                config.set("EigeneInsel", true);


                saveIslandConfig(player, config);
                Main.setWorldBorder(player);

                World world = Bukkit.getWorld("OneBlock");
                if (world != null) {
                    player.teleport(new Location(world, pos, 101, pos));
                }
                player.sendMessage(prefix + Objects.requireNonNull(Main.config.getString("islandjoinmessage.notowned")));
            } else {
                World world = Bukkit.getWorld("OneBlock");
                if (world != null) {
                    player.teleport(new Location(world, config.getInt("IslandSpawn-x"), 101, config.getInt("IslandSpawn-z")));
                    Main.setWorldBorder(player);
                    player.sendMessage(prefix + Objects.requireNonNull(Main.config.getString("islandjoinmessage.join")));
                } else {
                    player.sendMessage("§cOneBlock-Welt nicht gefunden!");
                }
            }
            return true;
        }
        player.sendMessage("§cNutze: /ob join");
        return true;
    }

    // Island Datei bekommen wo die userdaten gespeichert werden
    public static File getIslandFile(Player player) {
        return new File(Main.islandDataFolder, player.getName() + ".yml");
    }

    // Island Config laden (wird halt auch erstellt wenn es sie nd gibt)
    public static YamlConfiguration getIslandConfig(Player player) {
        File file = getIslandFile(player);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    // Island Config speichern//
    public static void saveIslandConfig(Player player, YamlConfiguration config) {
        try {
            config.save(getIslandFile(player));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<String> getAllIslandOwners() {
        List<String> owners = new ArrayList<>();
        File folder = Main.islandDataFolder;
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    String ownerName = file.getName().replace(".yml", "");
                    owners.add(ownerName);
                }
            }
        }
        return owners;
    }

    // Inselposition neu berechnen
    public static int getIslandCords(int x) {
        x = config.getInt("InselPadding.value") + 400;
        config.set("InselPadding.value", x);
        Main.getInstance().saveConfig();
        return x;
    }

    // Insel löschen die sachen in der config auch
    public static void deleteIsland(Player player) {
        YamlConfiguration config = getIslandConfig(player);
        World world = Bukkit.getWorld("OneBlock");

        if (world == null) {
            player.sendMessage(prefix + "§4OneBlock-Welt nicht gefunden.");
            return;
        }

        if (!config.getBoolean("EigeneInsel", false)) {
            player.sendMessage(prefix + "§aDu besitzt keine Insel.");
            return;
        }

        int x = config.getInt("OneBlock-x");
        int z = config.getInt("OneBlock-z");
        int size = config.getInt("WorldBorderSize", 50);

        player.teleport(new Location(world, 0, 100, 0));

        // Insel area löschen
        for (int dx = -size / 2; dx <= size / 2; dx++) {
            for (int dz = -size / 2; dz <= size / 2; dz++) {
                for (int dy = 90; dy <= 110; dy++) {
                    world.getBlockAt(x + dx, dy, z + dz).setType(Material.AIR);
                }
            }
        }

        // Config zurücksetzen
        config.set("EigeneInsel", false);
        config.set("IslandLevel", 1);
        config.set("MissingBlocksToLevelUp", 200);
        config.set("TotalBlocks", 200);
        config.set("IslandSpawn-x", null);
        config.set("IslandSpawn-z", null);
        config.set("x-position", null);
        config.set("z-position", null);
        config.set("OneBlock-x", null);
        config.set("OneBlock-z", null);
        config.set("WorldBorderSize", 50);

        saveIslandConfig(player, config);
        player.sendMessage(prefix + "§aDeine Insel wurde vollständig gelöscht.");
    }


    public static void visitIsland(Player visitor, String ownerName) {
        File file = new File(Main.islandDataFolder, ownerName + ".yml");

        if (!file.exists()) {
            visitor.sendMessage("§cDie Insel von §e" + ownerName + " §cwurde nicht gefunden.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);


        List<String> deniedUser = config.getStringList("denied");

        if (deniedUser.contains(visitor.getName())) {
            String deniedMessage = config.getString("DeniedMessage", "§cDu darfst diese Insel nicht betreten.");
            visitor.sendMessage(deniedMessage);
            return;
        }

        World world = Bukkit.getWorld("OneBlock");
        if (world == null) {
            visitor.sendMessage("§cDie Welt 'OneBlock' konnte nicht gefunden werden.");
            return;
        }

        int x = config.getInt("IslandSpawn-x");
        int z = config.getInt("IslandSpawn-z");
        visitor.teleport(new Location(world, x, 101, z));
        visitor.sendMessage("§aDu wurdest zur Insel von §e" + ownerName + " §ateleportiert.");
    }


    //nsel rebirth
    public static void rebirthIsland(Player player) {
        YamlConfiguration config = getIslandConfig(player);

        config.set("IslandLevel", 1);
        config.set("TotalBlocks", 200);
        config.set("MissingBlocksToLevelUp", 200);

        ItemStack stack = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName("§cRebirth Pickaxe");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(" ");
        lore.add(ChatColor.WHITE + "Diese Spitzhacke bekommst du beim Rebirth");
        lore.add("");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.EFFICIENCY, 6, true);
        meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addEnchant(Enchantment.FORTUNE, 2, true);
        stack.setItemMeta(meta);

        player.getInventory().addItem(stack);

        player.sendMessage("§aDeine Insel wurde erfolgreich Rebirthed");
        saveIslandConfig(player, config);
    }

    // Spieler zur Insel hinzufügen (added)
    public static void addPlayerToIsland(Player owner, OfflinePlayer toAdd) {
        YamlConfiguration config = getIslandConfig(owner);
        List<String> addedList = config.getStringList("added");

        String name = toAdd.getName();
        if (name == null) return;

        if (!addedList.contains(name)) {
            addedList.add(name);
            config.set("invited", addedList);
            saveIslandConfig(owner, config);
            owner.sendMessage(prefix + "§a" + name + " wurde zur Insel hinzugefügt.");

            if (toAdd.isOnline()) {
                toAdd.getPlayer().sendMessage(prefix + "§e" + owner.getName() + " hat dich auf seine Insel eingeladen. Nutze §a/ob accept§e um anzunehmen.");
            }
        } else {
            owner.sendMessage(prefix + Objects.requireNonNull(Main.config.getString("addmessagealreadyadded")));
        }
    }


    public static void trustPlayer(Player owner, OfflinePlayer target) {
        YamlConfiguration config = getIslandConfig(owner);
        List<String> trustedList = config.getStringList("trusted");

        String targetName = target.getName();
        if (targetName == null) {
            owner.sendMessage(prefix + "§cFehler: Spielername konnte nicht ermittelt werden.");
            return;
        }

        if (!trustedList.contains(targetName)) {
            trustedList.add(targetName);
            config.set("invitedtrust", trustedList);
            saveIslandConfig(owner, config);

            String trustMsg = config.getString("trustmessage");
            if (trustMsg == null) trustMsg = "Du hast %player% als vertrauenswürdigen Spieler hinzugefügt.";
            owner.sendMessage(prefix + trustMsg.replace("%player%", targetName));

            if (target.isOnline() && target.getPlayer() != null) {
                target.getPlayer().sendMessage(prefix + "§e" + owner.getName() + " hat dich auf seine Insel eingeladen. Nutze §a/ob accept§e um anzunehmen.");
            }
        } else {
            String alreadyAddedMsg = config.getString("addmessagealreadyadded");
            if (alreadyAddedMsg == null) alreadyAddedMsg = "Der Spieler ist bereits vertrauenswürdig.";
            owner.sendMessage(prefix + alreadyAddedMsg);
        }
    }



    // Einladung annehmens
    public static void acceptInvite(Player player) {
        File folder = Main.islandDataFolder;
        File[] islandFiles = folder.listFiles();

        if (islandFiles == null) {
            player.sendMessage("§cKeine Inseln gefunden.");
            return;
        }

        for (File file : islandFiles) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<String> invited = config.getStringList("invited");
            List<String> invitedTrust = config.getStringList("invitedtrust");
            List<String> added = config.getStringList("added");
            List<String> trusted = config.getStringList("trusted");
            String playerName = player.getName();
            String ownerName = config.getString("owner");

            boolean accepted = false;

            if (invited.contains(playerName)) { //das wird bei /ob add ausgeführt
                invited.remove(playerName);
                if (!added.contains(playerName)) added.add(playerName);
                config.set("invited", invited);
                config.set("added", added);
                player.sendMessage(prefix + " §aDu bist jetzt Mitglied auf der Insel von §e" + ownerName + "§a.");
                accepted = true;
            } else if (invitedTrust.contains(playerName)) { //das bei ob trust habs nd besser hinbekommen
                invitedTrust.remove(playerName);
                if (!trusted.contains(playerName)) trusted.add(playerName);
                config.set("invitedtrust", invitedTrust);
                config.set("trusted", trusted);
                player.sendMessage(prefix + " §aDu wurdest als vertrauenswürdiger Spieler auf der Insel von §e" + ownerName + "§a hinzugefügt.");
                accepted = true;
            }

            if (accepted) {
                try {
                    config.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    player.sendMessage(" §cFehler beim Speichern der Insel-Daten.");
                }
                return;
            }
        }

        player.sendMessage(prefix + " §cDu hast keine offenen Einladungen.");
    }

    public static void denyfromisland(Player owner, OfflinePlayer target) {
        YamlConfiguration config = getIslandConfig(owner);
        List<String> denied = config.getStringList("denied");

        if (!denied.contains(target.getName())) {
            denied.add(target.getName());
            config.set("denied", denied);
            saveIslandConfig(owner, config);
            owner.sendMessage(prefix + Main.config.getString("banmessage").replace("%player%", target.getName()));

            if (target.isOnline() && target.getPlayer() != null) {
                target.getPlayer().sendMessage(prefix + "§cDu wurdest auf der Insel von " + owner.getName() + " gebannt.");
            }
        } else {
            owner.sendMessage(prefix + "§cDieser Spieler ist bereits gebannt.");
        }
    }


    public static void unban(Player owner, Player target) {
        YamlConfiguration config = getIslandConfig(owner);
        List<String> denied = config.getStringList("denied");

        if (denied.contains(target.getName())) {
            denied.remove(target.getName());
            config.set("denied", denied);
            saveIslandConfig(owner, config);
            owner.sendMessage(prefix + "§a" + target.getName() + " wurde von der Bannliste entfernt.");
        } else {
            owner.sendMessage(prefix + "§c" + target.getName() + " war nicht gebannt.");
        }
    }

    public static void remove(Player owner, Player target) {
        YamlConfiguration config = getIslandConfig(owner);
        List<String> added = config.getStringList("added");
        List<String> trusted = config.getStringList("trusted");

        boolean wasMember = false;

        if (added.contains(target.getName())) {
            added.remove(target.getName());
            config.set("added", added);
            wasMember = true;
        }

        if (trusted.contains(target.getName())) {
            trusted.remove(target.getName());
            config.set("trusted", trusted);
            wasMember = true;
        }

        if (wasMember) {
            saveIslandConfig(owner, config);
            owner.sendMessage(prefix + "§a" + target.getName() + " wurde von deiner Insel entfernt.");
            if (target.isOnline()) {
                target.getPlayer().sendMessage(prefix + "§cDu wurdest von der Insel von " + owner.getName() + " entfernt.");
            }
        } else {
            owner.sendMessage(prefix + "§c" + target.getName() + " ist kein Mitglied deiner Insel.");
        }
    }

    public static void leaveIsland(Player player, String ownerName) {
        String prefix = Main.config.getString("Server");
        File islandFile = new File(Main.islandDataFolder, ownerName + ".yml");
        if (!islandFile.exists()) {
            player.sendMessage(prefix + "§cDie Insel von " + ownerName + " wurde nicht gefunden.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(islandFile);

        List<String> added = config.getStringList("added");
        List<String> trusted = config.getStringList("trusted");

        boolean changed = false;

        if (added.contains(player.getName())) {
            added.remove(player.getName());
            config.set("added", added);
            changed = true;
        }
        if (trusted.contains(player.getName())) {
            trusted.remove(player.getName());
            config.set("trusted", trusted);
            changed = true;
        }

        if (changed) {
            try {
                config.save(islandFile);
                player.sendMessage(prefix + "§aDu hast die Insel von §e" + ownerName + "§a verlassen.");
            } catch (IOException e) {
                e.printStackTrace();
                player.sendMessage(prefix + "§cFehler beim Verlassen der Insel.");
            }
        } else {
            player.sendMessage(prefix + "§cDu bist auf der Insel von §e" + ownerName + " §nichts eingetragen.");
        }
    }

}