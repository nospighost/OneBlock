package de.Main.OneBlock.Manager;

import de.Main.OneBlock.Main;
import de.Main.OneBlock.Player.PlayerListener;
import de.Main.OneBlock.database.MoneyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static de.Main.OneBlock.Main.*;

public class Manager implements Listener {
    public static Economy economy;
    private static JavaPlugin plugin;
    static String prefix = Main.config.getString("Server");

    public Manager(Economy eco, JavaPlugin plugin) {
        economy = eco;
        this.plugin = plugin;
    }

    public static boolean createOrJoinIsland(Player player, String[] args) {
        UUID uuid = player.getUniqueId();


        if (args.length == 1 && args[0].equalsIgnoreCase("join")) {

            if (MoneyManager.getBoolean(uuid, "EigeneInsel", false) != true) {
                player.sendMessage(prefix + Main.config.getString("islandjoinmessage.create"));
                int padding = Main.config.getInt("value");
                int pos = getIslandCords(padding);
                MoneyManager.setInt(uuid, "OneBlock_x", pos);
                MoneyManager.setInt(uuid, "OneBlock_z", pos);
                MoneyManager.setInt(uuid, "x_position", pos);
                MoneyManager.setInt(uuid, "z_position", pos);
                MoneyManager.setInt(uuid, "IslandSpawn_x", pos);
                MoneyManager.setInt(uuid, "IslandSpawn_z", pos);
                MoneyManager.setInt(uuid, "WorldBorderSize", 50);
                MoneyManager.setBoolean(uuid, "EigeneInsel", true);
                MoneyManager.setString(uuid, "owner", player.getName());


                World world = Bukkit.getWorld("OneBlock");
                if (world != null) {
                    Location tp = new Location(world, pos, 101, pos);
                    Location blockLoc = new Location(
                            world,
                            MoneyManager.getInt(uuid, "OneBlock_x", pos),
                            100,
                            MoneyManager.getInt(uuid, "OneBlock_z", pos)
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
                int spawnX = MoneyManager.getInt(uuid, "IslandSpawn_x", 0);
                int spawnZ = MoneyManager.getInt(uuid, "IslandSpawn_z", 0);
                Location tp = new Location(world, spawnX, 101, spawnZ);
                Location blockLoc = new Location(
                        world,
                        MoneyManager.getInt(uuid, "OneBlock_x", 0),
                        100,
                        MoneyManager.getInt(uuid, "OneBlock_z", 0)
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
    public static File getIslandFile(UUID uuid) {
        return new File(Main.islandDataFolder, uuid.toString() + ".yml");
    }


    public static String getIslandOwnerByLocation(Location loc) {
        File folder = Main.islandDataFolder;
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    int centerX = config.getInt("x-position");
                    int centerZ = config.getInt("z-position");
                    int borderSize = config.getInt("WorldBorderSize", 50);

                    int halfSize = borderSize / 2;
                    if (loc.getWorld().getName().equals("OneBlock") &&
                            loc.getX() >= centerX - halfSize && loc.getX() <= centerX + halfSize &&
                            loc.getZ() >= centerZ - halfSize && loc.getZ() <= centerZ + halfSize) {
                        return file.getName().replace(".yml", "");
                    }
                }
            }
        }
        return null;
    }


    public static YamlConfiguration getIslandConfig(UUID uuid) {
        File file = getIslandFile(uuid);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }


    public static void saveIslandConfig(UUID uuid, YamlConfiguration config) {
        try {
            config.save(getIslandFile(uuid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<UUID> getAllIslandOwners() {
        List<UUID> owners = new ArrayList<>();
        File folder = Main.islandDataFolder;
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    try {
                        owners.add(UUID.fromString(file.getName().replace(".yml", "")));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }
        return owners;
    }

    public static int getIslandCords(int x) {
        x = config.getInt("InselPadding.value") + 400;
        config.set("InselPadding.value", x);
        Main.getInstance().saveConfig();
        return x;
    }

    public static void deleteIsland(Player player) {
        YamlConfiguration config = getIslandConfig(player.getUniqueId());
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

        for (int dx = -size / 2; dx <= size / 2; dx++) {
            for (int dz = -size / 2; dz <= size / 2; dz++) {
                for (int dy = 90; dy <= 110; dy++) {
                    world.getBlockAt(x + dx, dy, z + dz).setType(Material.AIR);
                }
            }
        }

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
        config.set("Durchgespielt", false);

        saveIslandConfig(player.getUniqueId(), config);
        player.sendMessage(prefix + "§aDeine Insel wurde vollständig gelöscht.");
    }

    public static void visitIsland(Player visitor, String ownerNameOrUUID) {
        UUID ownerUUID = null;


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


        File file = getIslandFile(ownerUUID);

        if (!file.exists()) {
            visitor.sendMessage("§cDie Insel wurde nicht gefunden.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<String> denied = config.getStringList("denied");
        if (denied.contains(visitor.getUniqueId().toString())) {
            String msg = config.getString("DeniedMessage", "§cDu darfst diese Insel nicht betreten.");
            visitor.sendMessage(msg);
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

        String name = Bukkit.getOfflinePlayer(ownerUUID).getName();
        visitor.sendMessage("§aDu wurdest zur Insel von §e" + name + " §ateleportiert.");
    }


    public static void rebirthIsland(Player player) {
        YamlConfiguration config = getIslandConfig(player.getUniqueId());

        config.set("IslandLevel", 1);
        config.set("TotalBlocks", 200);
        config.set("MissingBlocksToLevelUp", 200);
        config.set("Durchgespielt", false);

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
        saveIslandConfig(player.getUniqueId(), config);
    }

    public static void trustPlayer(Player owner, OfflinePlayer target) {
        YamlConfiguration config = getIslandConfig(owner.getUniqueId());
        List<String> trustedList = config.getStringList("trusted");

        String uuidStr = target.getUniqueId().toString();

        if (!trustedList.contains(uuidStr)) {
            trustedList.add(uuidStr);
            config.set("invitedtrust", trustedList);
            saveIslandConfig(owner.getUniqueId(), config);

            String msg = config.getString("trust.trustmessage", "Du hast %player% als vertrauenswürdigen Spieler hinzugefügt.");
            owner.sendMessage(prefix + msg.replace("%player%", target.getName()));

            if (target.isOnline()) {
                target.getPlayer().sendMessage(prefix + "§e" + owner.getName() + " hat dich auf seine Insel eingeladen. Nutze §a/ob accept§e um anzunehmen.");
            }
        } else {
            owner.sendMessage(prefix + Main.config.getString("trust.trustmessagealready"));
        }
    }

    public static void acceptInvite(Player player) {
        File[] files = Main.islandDataFolder.listFiles();
        if (files == null) {
            player.sendMessage("§cKeine Inseln gefunden.");
            return;
        }

        String uuidStr = player.getUniqueId().toString();

        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<String> invited = config.getStringList("invited");
            List<String> invitedTrust = config.getStringList("invitedtrust");
            List<String> added = config.getStringList("added");
            List<String> trusted = config.getStringList("trusted");
            String ownerId = config.getString("owner");

            boolean accepted = false;

            if (invited.contains(uuidStr)) {
                invited.remove(uuidStr);
                if (!added.contains(uuidStr)) added.add(uuidStr);
                config.set("invited", invited);
                config.set("added", added);
                accepted = true;
            } else if (invitedTrust.contains(uuidStr)) {
                invitedTrust.remove(uuidStr);
                if (!trusted.contains(uuidStr)) trusted.add(uuidStr);
                config.set("invitedtrust", invitedTrust);
                config.set("trusted", trusted);
                accepted = true;
            }

            if (accepted) {

                OfflinePlayer ownerPlayer;
                try {
                    UUID ownerUUID = UUID.fromString(ownerId);
                    ownerPlayer = Bukkit.getOfflinePlayer(ownerUUID);
                } catch (IllegalArgumentException e) {
                    ownerPlayer = Bukkit.getOfflinePlayer(ownerId);
                }

                String ownerName = ownerPlayer.getName() != null ? ownerPlayer.getName() : "Unbekannt";

                if (invited.contains(uuidStr)) {
                    player.sendMessage(prefix + " §aDu bist jetzt Mitglied auf der Insel von §e" + ownerName + "§a.");
                } else {
                    player.sendMessage(prefix + " §aDu wurdest als vertrauenswürdiger Spieler auf der Insel von §e" + ownerName + "§a hinzugefügt.");
                }

                try {
                    config.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    player.sendMessage("§cFehler beim Speichern.");
                }

                return;
            }
        }

        player.sendMessage(prefix + " §cDu hast keine offenen Einladungen.");
    }


    public static void denyfromisland(Player owner, OfflinePlayer target) {
        YamlConfiguration config = getIslandConfig(owner.getUniqueId());
        List<String> denied = config.getStringList("denied");
        String uuidStr = target.getUniqueId().toString();


        if (!denied.contains(uuidStr)) {
            denied.add(uuidStr);
            config.set("denied", denied);
            saveIslandConfig(owner.getUniqueId(), config);
            owner.sendMessage(prefix + Main.config.getString("banmessage").replace("%player%", target.getName()));
            if (target.isOnline()) {
                target.getPlayer().sendMessage(prefix + Main.config.getString("playergetdeniedmessage").replace("%player%", target.getName()));
            }
        } else {
            owner.sendMessage(prefix + "§cDieser Spieler ist bereits gebannt.");
        }
    }

    public static void unban(Player owner, Player target) {
        YamlConfiguration config = getIslandConfig(owner.getUniqueId());
        List<String> denied = config.getStringList("denied");
        String uuidStr = target.getUniqueId().toString();

        if (denied.contains(uuidStr)) {
            denied.remove(uuidStr);
            config.set("denied", denied);
            saveIslandConfig(owner.getUniqueId(), config);
            owner.sendMessage(prefix + "§a" + target.getName() + " wurde entbannt.");
        } else {
            owner.sendMessage(prefix + "§c" + target.getName() + " war nicht gebannt.");
        }
    }

    public static void remove(Player owner, Player target) {
        YamlConfiguration config = getIslandConfig(owner.getUniqueId());
        List<String> added = config.getStringList("added");
        List<String> trusted = config.getStringList("trusted");
        String uuidStr = target.getUniqueId().toString();

        boolean removed = added.remove(uuidStr) | trusted.remove(uuidStr);

        if (removed) {
            config.set("added", added);
            config.set("trusted", trusted);
            saveIslandConfig(owner.getUniqueId(), config);
            owner.sendMessage(prefix + "§a" + target.getName() + " wurde entfernt.");
            if (target.isOnline()) {
                target.getPlayer().sendMessage(prefix + "§cDu wurdest von der Insel entfernt.");
            }
        } else {
            owner.sendMessage(prefix + "§c" + target.getName() + " ist kein Mitglied.");
        }
    }

    public static void leaveIsland(Player player, String ownerNameOrUUID) {
        UUID ownerUUID = null;

        try {
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

        File islandFile = getIslandFile(ownerUUID);

        if (!islandFile.exists()) {
            player.sendMessage(prefix + "§cDie Insel wurde nicht gefunden.");
            return;
        }

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(islandFile);
            String uuidStr = player.getUniqueId().toString();

            List<String> added = config.getStringList("added");
            List<String> trusted = config.getStringList("trusted");

            boolean changed = added.remove(uuidStr) | trusted.remove(uuidStr);

            if (changed) {
                config.set("added", added);
                config.set("trusted", trusted);
                config.save(islandFile);
                player.sendMessage(prefix + "§aDu hast die Insel verlassen.");
            } else {
                player.sendMessage(prefix + "§cDu bist kein Mitglied dieser Insel.");
            }
        } catch (IOException e) {
            player.sendMessage(prefix + "§cFehler beim Verlassen der Insel.");
        }
    }


    public static void declineinvite(Player player, String targetName) {

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);
        UUID targetUUID = targetPlayer.getUniqueId();
        String uuidStr = targetUUID.toString();

        YamlConfiguration config = getIslandConfig(player.getUniqueId());
        List<String> invited = config.getStringList("invited");
        List<String> invitedTrust = config.getStringList("invitedtrust");

        boolean removed = false;

        if (invited.contains(uuidStr)) {
            invited.remove(uuidStr);
            removed = true;
            config.set("invited", invited);
        }

        if (invitedTrust.contains(uuidStr)) {
            invitedTrust.remove(uuidStr);
            removed = true;
            config.set("invitedtrust", invitedTrust);
        }

        if (removed) {
            saveIslandConfig(player.getUniqueId(), config);


            player.sendMessage(Main.config.getString("trust.declinetrustself").replace("%player%", targetName));


            Player inviter = Bukkit.getPlayer(targetUUID);
            if (inviter != null) {
                inviter.sendMessage(Main.config.getString("trust.declinetrust").replace("%player%", player.getName()));
            }
        } else {
            player.sendMessage(Main.config.getString("trust.declinetrustnotrust").replace("%player%", targetName));
        }
    }

    public static UUID getIslandOwnerUUIDByLocation(Location loc) {
        File folder = Main.islandDataFolder;
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    int centerX = config.getInt("x-position");
                    int centerZ = config.getInt("z-position");
                    int borderSize = config.getInt("WorldBorderSize", 50);

                    int halfSize = borderSize / 2;
                    if (loc.getWorld().getName().equals(PlayerListener.WORLD_NAME) &&
                            loc.getX() >= centerX - halfSize && loc.getX() <= centerX + halfSize &&
                            loc.getZ() >= centerZ - halfSize && loc.getZ() <= centerZ + halfSize) {

                        String ownerUUIDStr = config.getString("owner-uuid");
                        if (ownerUUIDStr != null) {
                            try {
                                return UUID.fromString(ownerUUIDStr);
                            } catch (IllegalArgumentException e) {

                            }
                        }
                    }
                }
            }
        }
        return null;
    }

}
