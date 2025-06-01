package de.Main.OneBlock.OneBlock.Manager;

import de.Main.OneBlock.Main;
import de.Main.OneBlock.NPC.Manager.NPCManager;
import de.Main.OneBlock.database.DBM;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

import static de.Main.OneBlock.Main.*;

public class Manager implements Listener {
    public static Economy eco;
    private static JavaPlugin plugin;
    static String prefix = Main.config.getString("Server");

    public Manager(Economy eco, JavaPlugin plugin) {
        eco = eco;
        this.plugin = plugin;
    }

    public static boolean createOrJoinIsland(Player player, String[] args) {
        UUID uuid = player.getUniqueId();


        if (args.length == 1 && args[0].equalsIgnoreCase("join")) {

            if (!DBM.getBoolean("userdata", uuid, "EigeneInsel", false)) {
                player.sendMessage(prefix + Main.config.getString("islandjoinmessage.create"));
                int padding = Main.config.getInt("value");
                int pos = getIslandCords(padding);
                DBM.setInt("userdata",uuid, "OneBlock_x", pos);
                DBM.setInt("userdata",uuid, "OneBlock_z", pos);
                DBM.setInt("userdata",uuid, "x_position", pos);
                DBM.setInt("userdata",uuid, "z_position", pos);
                DBM.setInt("userdata",uuid, "IslandSpawn_x", pos);
                DBM.setInt("userdata",uuid, "IslandSpawn_z", pos);
                DBM.setInt("userdata",uuid, "WorldBorderSize", 50);
                DBM.setBoolean("userdata",uuid, "EigeneInsel", true);
                DBM.setString("userdata",uuid, "owner", player.getName());



                World world = Bukkit.getWorld("OneBlock");
                if (world != null) {
                    Location NPC = new Location(world, pos, 101, pos);
                    NPCManager.createNPC(NPC);
                    Location tp = new Location(world, pos, 101, pos);
                    Location blockLoc = new Location(
                            world,
                            DBM.getInt("userdata", uuid, "OneBlock_x", pos),
                            100,
                            DBM.getInt("userdata", uuid, "OneBlock_z", pos)
                    );
                    Block block = blockLoc.getBlock();
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.OAK_LOG);
                    }
                    player.teleport(tp);
                } else {
                    player.sendMessage("§cOneBlock-Welt nicht gefunden!");
                }

                player.sendMessage(prefix + Main.config.getString("islandjoinmessage.notowned"));
                return true;
            }


            World world = Bukkit.getWorld("OneBlock");
            if (world != null) {
                int spawnX = DBM.getInt("userdata", uuid, "IslandSpawn_x", 0);
                int spawnZ = DBM.getInt("userdata", uuid, "IslandSpawn_z", 0);
                Location tp = new Location(world, spawnX, 101, spawnZ);
                Location blockLoc = new Location(
                        world,
                        DBM.getInt("userdata", uuid, "OneBlock_x", 0),
                        100,
                        DBM.getInt("userdata", uuid, "OneBlock_z", 0)
                );
                Block block = blockLoc.getBlock();
                if (block.getType() == Material.AIR) {
                    block.setType(Material.OAK_LOG);
                }
                player.teleport(tp);
            } else {
                player.sendMessage("§cOneBlock-Welt nicht gefunden!");
            }

            player.sendMessage(prefix + Main.config.getString("islandjoinmessage.join"));
            return true;
        }
        return false;
    }

    public static void deleteIsland(Player player) {
        UUID uuid = player.getUniqueId();
        World world = Bukkit.getWorld("OneBlock");
        if (world == null) {
            player.sendMessage(prefix + "§4OneBlock-Welt nicht gefunden.");
            return;
        }

        boolean hasIsland = DBM.getBoolean("userdata",uuid, "EigeneInsel", false);
        if (!hasIsland) {
            player.sendMessage(prefix + "§aDu besitzt keine Insel.");
            return;
        }

        int x = DBM.getInt("userdata", uuid, "OneBlock_x", 0);
        int z = DBM.getInt("userdata", uuid, "OneBlock_z", 0);
        int size = DBM.getInt("userdata", uuid, "WorldBorderSize", 50);


        player.teleport(new Location(world, 0, 100, 0));


        for (int dx = -size / 2; dx <= size / 2; dx++) {
            for (int dz = -size / 2; dz <= size / 2; dz++) {
                for (int dy = 90; dy <= 110; dy++) {
                    world.getBlockAt(x + dx, dy, z + dz).setType(Material.AIR);
                }
            }
        }

        // Spalten zurücksetzen
        DBM.setBoolean("userdata", uuid, "EigeneInsel", false);
        DBM.setInt("userdata", uuid, "IslandLevel", 1);
        DBM.setInt("userdata", uuid, "MissingBlocksToLevelUp", 200);
        DBM.setInt("userdata", uuid, "TotalBlocks", 200);
        DBM.setInt("userdata", uuid, "IslandSpawn_x", 0);
        DBM.setInt("userdata", uuid, "IslandSpawn_z", 0);
        DBM.setInt("userdata", uuid, "x_position", 0);
        DBM.setInt("userdata", uuid, "z_position", 0);
        DBM.setInt("userdata", uuid, "OneBlock_x", 0);
        DBM.setInt("userdata", uuid, "OneBlock_z", 0);
        DBM.setInt("userdata", uuid, "WorldBorderSize", 50);
        DBM.setBoolean("userdata", uuid, "Durchgespielt", false);
        player.sendMessage(prefix + "§aDeine Insel wurde vollständig gelöscht.");
    }

    public static void visitIsland(Player visitor, String ownerNameOrUUID) {
        UUID ownerUUID;
        try {
            ownerUUID = UUID.fromString(ownerNameOrUUID);
        } catch (IllegalArgumentException e) {
            OfflinePlayer ownerOffline = Bukkit.getOfflinePlayer(ownerNameOrUUID);
            if (ownerOffline != null && ownerOffline.hasPlayedBefore()) {
                ownerUUID = ownerOffline.getUniqueId();
            } else {
                visitor.sendMessage("§cSpieler oder Inselbesitzer nicht gefunden.");
                return;
            }
        }

        // Prüfen ob der Besitzer wirklich eine Insel betitzt
        if (!DBM.getBoolean("userdata", ownerUUID, "EigeneInsel", false)) {
            visitor.sendMessage("§cDie Insel wurde nicht gefunden.");
            return;
        }

        // Prüfen ob Besucher auf der Insel gebannt ist
        String deniedCSV = DBM.getString("userdata", ownerUUID, "denied", "");
        List<String> denied = csvToList(deniedCSV);

        if (denied.contains(visitor.getUniqueId().toString())) {
            String msg = Main.getInstance().getConfig().getString("DeniedMessage", "§cDu darfst diese Insel nicht betreten.");
            visitor.sendMessage(msg);
            return;
        }

        // Inselkoordinaten vom Besitzer
        int x = DBM.getInt("userdata", ownerUUID, "IslandSpawn_x", 0);
        int z = DBM.getInt("userdata", ownerUUID, "IslandSpawn_z", 0);

        World world = Bukkit.getWorld("OneBlock");
        if (world == null) {
            visitor.sendMessage("§cDie Welt 'OneBlock' konnte nicht gefunden werden.");
            return;
        }

        visitor.teleport(new Location(world, x, 101, z));

        String name = Bukkit.getOfflinePlayer(ownerUUID).getName();
        visitor.sendMessage("§aDu wurdest zur Insel von §e" + name + " §ateleportiert.");
    }


    public static void rebirthIsland(Player player) {
        UUID uuid = player.getUniqueId();

        DBM.setInt("userdata", uuid, "IslandLevel", 1);
        DBM.setInt("userdata", uuid, "TotalBlocks", 200);
        DBM.setInt("userdata", uuid, "MissingBlocksToLevelUp", 200);
        DBM.setBoolean("userdata", uuid, "Durchgespielt", false);

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
    }

    public static void acceptInvite(Player player) {
        UUID playerUUID = player.getUniqueId();
        String uuidStr = playerUUID.toString();
        List<UUID> allOwners = Main.getAllOwners();
        for (UUID ownerUUID : allOwners) {
            String invitedTrustCSV = DBM.getString("userdata", ownerUUID, "invited", "");
            String trustedCSV = DBM.getString("userdata", ownerUUID, "trusted", "");
            List<String> invitedTrust = csvToList(invitedTrustCSV);
            List<String> trusted = csvToList(trustedCSV);
            boolean accepted = false;
            if (invitedTrust.contains(uuidStr)) {
                invitedTrust.remove(uuidStr);
                if (!trusted.contains(uuidStr)) trusted.add(uuidStr);
                accepted = true;
            } else if (invitedTrust.contains(uuidStr)) {
                invitedTrust.remove(uuidStr);
                if (!trusted.contains(uuidStr)) trusted.add(uuidStr);
                accepted = true;
            }
            if (accepted) {
                DBM.setString("userdata", ownerUUID, "invited", listToCsv(invitedTrust));
                DBM.setString("userdata", ownerUUID, "trusted", listToCsv(trusted));
                OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(ownerUUID);
                String ownerName = ownerPlayer.getName() != null ? ownerPlayer.getName() : "Unbekannt";
                if (invitedTrust.contains(uuidStr)) {
                    player.sendMessage(prefix + " §aDu bist jetzt Mitglied auf der Insel von §e" + ownerName + "§a.");
                } else {
                    player.sendMessage(prefix + " §aDu wurdest als vertrauenswürdiger Spieler auf der Insel von §e" + ownerName + "§a hinzugefügt.");
                }

                return;
            }
        }

        player.sendMessage(prefix + " §cDu hast keine offenen Einladungen.");
    }

    public static void trustPlayer(Player owner, OfflinePlayer target) {
        UUID ownerUUID = owner.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        // aktuelle Liste der eingaldenen Spieler
        List<String> invitedList = new ArrayList<>(DBM.getStringList("userdata", ownerUUID, "invited"));
        String targetUUIDStr = targetUUID.toString();

        // Überprüfen obder Spieler bereits eingeladen ist
        if (!invitedList.contains(targetUUIDStr)) {
            invitedList.add(targetUUIDStr);
            DBM.setStringList("userdata", ownerUUID, "invited", invitedList);
            String msg = Main.config.getString(
                    "invite.invitemessage",
                    "Du hast %player% auf deine Insel eingeladen."
            );
            owner.sendMessage(prefix + msg.replace("%player%", target.getName()));

            // Nachricht an den Eingeladenen
            if (target.isOnline()) {
                target.getPlayer().sendMessage(prefix + "§e" + owner.getName() +
                        " hat dich auf seine Insel eingeladen. Nutze §a/ob accept§e, um die Einladung anzunehmen.");
            }
        } else {
            // Spieler ist bereits eingeladen
            owner.sendMessage(prefix + Main.config.getString("invite.invitemessagealready",
                    "Dieser Spieler wurde bereits eingeladen."));
        }
    }

    public static void denyfromisland(Player owner, OfflinePlayer target) {
        UUID ownerUUID = owner.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        String targetUUIDStr = targetUUID.toString();
        String deniedCSV = DBM.getString("userdata", ownerUUID, "denied", "");
        List<String> deniedList = csvToList(deniedCSV);

        if (!deniedList.contains(targetUUIDStr)) {
            deniedList.add(targetUUIDStr);//Spieler zur gesperrten Liste hinzufügen
            DBM.setString("userdata", ownerUUID, "denied", listToCsv(deniedList)); //In die Datenbank setzen

            // Nachricht an den Besitzer
            String banMessage = Main.config.getString("banmessage", "Spieler %player% wurde gebannt.")
                    .replace("%player%", target.getName() != null ? target.getName() : "Unbekannt");
            owner.sendMessage(prefix + banMessage);

            // Nachricht an den Zielspieler wenn er online ist online
            if (target.isOnline()) {
                Player targetPlayer = target.getPlayer();
                String deniedMessage = Main.config.getString("playergetdeniedmessage", "Du wurdest von der Insel von %player% gebannt.")
                        .replace("%player%", owner.getName() != null ? owner.getName() : "Unbekannt");
                targetPlayer.sendMessage(prefix + deniedMessage);
            }
        } else {
            owner.sendMessage(prefix + "§cDieser Spieler ist bereits gebannt.");
        }
    }

    public static void unban(Player owner, OfflinePlayer target) {
        UUID ownerUUID = owner.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        String targetUUIDStr = targetUUID.toString();


        String deniedCSV = DBM.getString("userdata", ownerUUID, "denied", "");   // aktuelle Denied liste holen
        List<String> deniedList = csvToList(deniedCSV);

        if (deniedList.contains(targetUUIDStr)) {
            // Spieler aus der "denied"-Liste entfernen
            deniedList.remove(targetUUIDStr);
            DBM.setString("userdata", ownerUUID, "denied", listToCsv(deniedList));

            // Nachricht an den Besitzer
            owner.sendMessage(prefix + "§a" + target.getName() + " wurde entbannt.");
        } else {
            owner.sendMessage(prefix + "§c" + (target.getName() != null ? target.getName() : "Unbekannt") + " war nicht gebannt.");
        }
    }

    public static void remove(Player owner, OfflinePlayer target) {
        UUID ownerUUID = owner.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        String targetUUIDStr = targetUUID.toString();

        // aktuellen trusted-Liste aus der Datenbank
        String trustedCSV = DBM.getString("userdata", ownerUUID, "trusted", "");
        List<String> trustedList = csvToList(trustedCSV);

        if (trustedList.remove(targetUUIDStr)) {
            //  Liste in der Datenbank speichern
            DBM.setString("userdata", ownerUUID, "trusted", listToCsv(trustedList));

            // Benachrichtigung an den Besitzer
            owner.sendMessage(prefix + "§a" + (target.getName() != null ? target.getName() : "Unbekannt") + " wurde von der Insel entfernt.");

            // Benachrichtigung an den Spieler wenn er online ist
            if (target.isOnline()) {
                target.getPlayer().sendMessage(prefix + "§cDu wurdest von der Insel entfernt.");
            }
        } else {
            // Spieler ist nicht auf der trusted Liste
            owner.sendMessage(prefix + "§c" + (target.getName() != null ? target.getName() : "Unbekannt") + " ist kein Mitglied.");
        }
    }


    public static void leaveIsland(Player player, String ownerNameOrUUID) {
        UUID ownerUUID;

        try {
          // Übergebenen wert versuchen zur UUID zu machen
            ownerUUID = UUID.fromString(ownerNameOrUUID);
        } catch (IllegalArgumentException e) {
            OfflinePlayer ownerOffline = Bukkit.getOfflinePlayer(ownerNameOrUUID);
            if (ownerOffline != null && ownerOffline.hasPlayedBefore()) {
                ownerUUID = ownerOffline.getUniqueId();
            } else {
                player.sendMessage(prefix + "§cSpieler oder Inselbesitzer nicht gefunden.");
                return;
            }
        }

        String trustedCSV = DBM.getString("userdata", ownerUUID, "trusted", "");
        List<String> trustedList = csvToList(trustedCSV);

        String playerUUIDStr = player.getUniqueId().toString();
        boolean changed =  trustedList.remove(playerUUIDStr);

        if (changed) {
            DBM.setString("userdata", ownerUUID, "trusted", listToCsv(trustedList));
            player.sendMessage(prefix + "§aDu hast die Insel verlassen.");
        } else {
            player.sendMessage(prefix + "§cDu bist kein Mitglied dieser Insel.");
        }
    }

    public static void declineInvite(Player player, String targetName) {
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            player.sendMessage(prefix + "§cDer Spieler '" + targetName + "' wurde nicht gefunden.");
            return;
        }

        UUID targetUUID = targetPlayer.getUniqueId();
        UUID playerUUID = player.getUniqueId();

        // Trust Liste
        String invited = DBM.getString("userdata", targetUUID, "invited", "");


        List<String> invitedTrustList = csvToList(invited);

        String playerUUIDStr = playerUUID.toString();
        boolean removed = false;

        // Entfernen aus den Litsen und gucken ob es in der Liste vorhanden ist
        if (invitedTrustList.contains(playerUUIDStr)) {
            invitedTrustList.remove(playerUUIDStr);
            removed = true;
            DBM.setString("userdata", targetUUID, "invited", listToCsv(invitedTrustList));

        }


        if (removed) {
           player.sendMessage(prefix + Main.config.getString("trust.declinetrustself").replace("%player%", targetName));

            if (targetPlayer.isOnline()) {
                Player inviter = Bukkit.getPlayer(targetUUID);
                if (inviter != null) {
                  inviter.sendMessage(prefix + Main.config.getString("trust.declinetrust").replace("%player%", player.getName()));
                }
            }
        } else {
           player.sendMessage(prefix + Main.config.getString("trust.declinetrustnotrust").replace("%player%", targetName));
        }
    }

    public static int getIslandCords(int x) {
        x = config.getInt("InselPadding.value") + 400;
        config.set("InselPadding.value", x);
        Main.getInstance().saveConfig();
        return x;
    }

    public static List<String> csvToList(String csv) {
        if (csv == null || csv.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(csv.split(",")));
    }

    private static String listToCsv(List<String> list) {
        return String.join(",", list);
    }
}
