package de.Main.OneBlock.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.Main.OneBlock.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class DBM implements Listener {
    private static final Gson gson = new Gson();


    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerUUID = event.getPlayer().getUniqueId().toString();
            SQLTabel.Condition userdatacondition = new SQLTabel.Condition("owner", player.getName());
        SQLTabel.Condition queststatuscondition = new SQLTabel.Condition("owner", player.getName());
        SQLTabel.Condition questcompletecondition = new SQLTabel.Condition("owner", player.getName());

        if (!tabel.exits("userdata", userdatacondition)) {
            //userdata
            tabel.set("userdata", "owner", player.getName(), userdatacondition);
            tabel.set("userdata", "WorldBorderSize", 50, userdatacondition);
            tabel.set("userdata", "TotalBlocks", 0, userdatacondition);
            tabel.set("userdata", "EigeneInsel", false, userdatacondition);
            tabel.set("userdata", "IslandLevel", 1, userdatacondition);
            tabel.set("userdata", "Durchgespielt", false, userdatacondition);
            tabel.set("userdata", "owner_uuid", playerUUID, userdatacondition);
            tabel.set("userdata", "MissingBlocksToLevelUp", 0, userdatacondition);
            tabel.set("userdata", "OneBlock_x", 0, userdatacondition);
            tabel.set("userdata", "OneBlock_z", 0, userdatacondition);
            tabel.set("userdata", "IslandSpawn_x", 0, userdatacondition);
            tabel.set("userdata", "IslandSpawn_z", 0, userdatacondition);
            tabel.set("userdata", "x_position", 0, userdatacondition);
            tabel.set("userdata", "z_position", 0, userdatacondition);
            tabel.set("userdata", "BorderParticle", "COMPOSTER", userdatacondition);
            tabel.set("userdata", "IslandBiom", "PLAINS", userdatacondition);
            tabel.set("userdata", "MobSpawning", true, userdatacondition);
            List<String> trustedList = new ArrayList<>();
            tabel.set("userdata", "trusted", String.join(",", trustedList), userdatacondition);
            List<String> deniedList = new ArrayList<>();
            tabel.set("userdata", "denied", String.join(",", deniedList), userdatacondition);
            List<String> invitedList = new ArrayList<>();
            tabel.set("userdata", "invited", String.join(",", invitedList), userdatacondition);
        }
        if (!tabel.exits("questcomplete", questcompletecondition)) {
            //quest
            tabel.set("questcomplete", "owner", player.getName(), questcompletecondition);
            tabel.set("questcomplete", "owner_uuid", playerUUID, questcompletecondition);
            tabel.set("questcomplete", "broken100stone", false, questcompletecondition);
            tabel.set("questcomplete", "broken200stone", false, questcompletecondition);
            tabel.set("questcomplete", "broken300stone", false, questcompletecondition);
        }
        if (!tabel.exits("queststatus", queststatuscondition)) {
            //quest
            tabel.set("queststatus", "owner", player.getName(), queststatuscondition);
            tabel.set("queststatus", "owner_uuid", playerUUID, queststatuscondition);
            tabel.set("queststatus", "broken100stone", false, queststatuscondition);
            tabel.set("queststatus", "broken200stone", false, queststatuscondition);
            tabel.set("queststatus", "broken300stone", false, queststatuscondition);
        }
    }


    private static SQLTabel tabel;

    public DBM(Main pl) {
        HashMap<String, SQLDataType> userdatacolumns = new HashMap<>();
        HashMap<String, SQLDataType> questcompletecolumns = new HashMap<>();
        HashMap<String, SQLDataType> queststatuscolumns = new HashMap<>();
        userdatacolumns.put("owner", SQLDataType.CHAR);
        userdatacolumns.put("WorldBorderSize", SQLDataType.INT);
        userdatacolumns.put("TotalBlocks", SQLDataType.INT);
        userdatacolumns.put("denied", SQLDataType.TEXT);
        userdatacolumns.put("invited", SQLDataType.TEXT);
        userdatacolumns.put("trusted", SQLDataType.TEXT);
        userdatacolumns.put("owner_uuid", SQLDataType.CHAR);
        userdatacolumns.put("EigeneInsel", SQLDataType.BOOLEAN);
        userdatacolumns.put("MissingBlocksToLevelUp", SQLDataType.INT);
        userdatacolumns.put("IslandLevel", SQLDataType.INT);
        userdatacolumns.put("Durchgespielt", SQLDataType.BOOLEAN);
        userdatacolumns.put("OneBlock_x", SQLDataType.INT);
        userdatacolumns.put("OneBlock_z", SQLDataType.INT);
        userdatacolumns.put("IslandSpawn_x", SQLDataType.INT);
        userdatacolumns.put("IslandSpawn_z", SQLDataType.INT);
        userdatacolumns.put("z_position", SQLDataType.INT);
        userdatacolumns.put("x_position", SQLDataType.INT);
        userdatacolumns.put("BorderParticle", SQLDataType.CHAR);
        userdatacolumns.put("IslandBiom", SQLDataType.CHAR);
        userdatacolumns.put("MobSpawning", SQLDataType.BOOLEAN);

        questcompletecolumns.put("owner", SQLDataType.CHAR);
        questcompletecolumns.put("owner_uuid", SQLDataType.CHAR);
        questcompletecolumns.put("broken100stone", SQLDataType.BOOLEAN);
        questcompletecolumns.put("broken200stone", SQLDataType.BOOLEAN);
        questcompletecolumns.put("broken300stone", SQLDataType.BOOLEAN);

        queststatuscolumns.put("owner", SQLDataType.CHAR);
        queststatuscolumns.put("owner_uuid", SQLDataType.CHAR);
        queststatuscolumns.put("broken100stone", SQLDataType.BOOLEAN);
        queststatuscolumns.put("broken200stone", SQLDataType.BOOLEAN);
        queststatuscolumns.put("broken300stone", SQLDataType.BOOLEAN);
        tabel = new SQLTabel(pl.getConnection(), "userdata", userdatacolumns);
        tabel = new SQLTabel(pl.getConnection(), "questcomplete", questcompletecolumns);
        tabel = new SQLTabel(pl.getConnection(), "queststatus", queststatuscolumns);
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    public static void setInt(String table, UUID uuid, String columnName, int value) {
        SQLTabel.Condition cond = new SQLTabel.Condition("owner_uuid", uuid.toString());
        tabel.set(table, columnName, value, cond);
    }

    public static void setString(String table, UUID uuid, String columnName, String value) {
        SQLTabel.Condition cond = new SQLTabel.Condition("owner_uuid", uuid.toString());
        tabel.set(table, columnName, value, cond);
    }

    public static void setBoolean(String table, UUID uuid, String columnName, boolean value) {
        SQLTabel.Condition cond = new SQLTabel.Condition("owner_uuid", uuid.toString());
        tabel.set(table, columnName, value, cond);
    }

    public static void setList(String table, UUID uuid, String columnName, List<String> list) {
        SQLTabel.Condition cond = new SQLTabel.Condition("owner_uuid", uuid.toString());
        String csv = String.join(",", list);
        tabel.set(table, columnName, csv, cond);
    }

    public static String getString(String table, UUID uuid, String columnName, String defaultValue) {
        SQLTabel.Condition condition = new SQLTabel.Condition("owner_uuid", uuid.toString());
        if (tabel.exits("userdata", condition)) {
            return tabel.getString(table, columnName, condition);
        }
        tabel.set(table, columnName, defaultValue, condition);
        return defaultValue;
    }

    public static int getInt(String table, UUID uuid, String columnName, int defaultValue) {
        SQLTabel.Condition condition = new SQLTabel.Condition("owner_uuid", uuid.toString());
        if (tabel.exits("userdata", condition)) {
            return tabel.getInt(table, columnName, condition);
        }
        tabel.set(table, columnName, defaultValue, condition);
        return defaultValue;
    }

    public static boolean getBoolean(String table, UUID uuid, String columnName, boolean defaultValue) {
        SQLTabel.Condition condition = new SQLTabel.Condition("owner_uuid", uuid.toString());
        if (tabel.exits(table, condition)) {
            return tabel.getBoolean(table, columnName, condition);
        }
        tabel.set(table, columnName, defaultValue, condition);
        return defaultValue;
    }

    public static List<String> getList(UUID ownerUUID, String key, List<String> defaultList) {
        String json = getJsonFromDB(ownerUUID, key);
        if (json == null || json.isEmpty()) {
            return defaultList;
        }

        try {
            JsonElement element = JsonParser.parseString(json);
            if (!element.isJsonArray()) {
                return defaultList;
            }
            return gson.fromJson(json, new TypeToken<List<String>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return defaultList;
        }
    }

    private static String getJsonFromDB(UUID ownerUUID, String key) {
        // Hier musst du deinen echten Code zum Laden einfügen
        // Zum Beispiel: return database.get(ownerUUID, key);
        return null;
    }

    public static UUID getUUID(String table, UUID userUUID, String key, UUID defaultValue) {
        try {
            String uuidString = getString(table, userUUID, key, null);
            if (uuidString != null && !uuidString.isEmpty()) {
                return UUID.fromString(uuidString);
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Ungültige UUID für " + key + " von " + userUUID + ": " + e.getMessage());
        }
        return defaultValue;
    }

    public static List<String> getStringList(String table, UUID uuid, String column) {
        String listAsString = getString(table, uuid, column, ""); // Liste als CSV-String abrufen  (in komma umgewandlete Strings )
        if (listAsString.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(listAsString.split(",")); // CSV-String in Liste umwandeln  (in komma umgewandlete Strings )
    }

    public static void setStringList(String table, UUID uuid, String column, List<String> values) {
        String listAsString = String.join(",", values); // Liste in CSV-String umwandeln (in komma umgewandlete Strings )
        setString(table, uuid, column, listAsString); //In die Datenbank
    }

    public static int getTotalBlocks() {
        int totalBlocks = -1;
        Connection connection = null; // Connection bleibt offen
        try {
            connection = Main.getInstance().getConnection().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        String sql = "SELECT TotalBlocks FROM userdata LIMIT 1"; // Beispielquery, ggf. anpassen

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                totalBlocks = rs.getInt("TotalBlocks");
            }

        } catch (SQLException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Fehler beim Abrufen von TotalBlocks", e);
        }

        return totalBlocks;
    }


}
