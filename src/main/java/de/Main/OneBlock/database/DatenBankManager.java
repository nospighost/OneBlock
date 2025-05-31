package de.Main.OneBlock.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.Main.OneBlock.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class DatenBankManager implements Listener {
    private static final Gson gson = new Gson();




    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerUUID = event.getPlayer().getUniqueId().toString();
        SQLTabel.Condition condition = new SQLTabel.Condition("owner", player.getName());

        if (!tabel.exits(condition)) {
            tabel.set("owner", player.getName(), condition);
            tabel.set("WorldBorderSize", 50, condition);
            tabel.set("TotalBlocks", 0, condition);
            tabel.set("EigeneInsel", false, condition);
            tabel.set("IslandLevel", 1, condition);
            tabel.set("Durchgespielt", false, condition);
            tabel.set("owner_uuid", playerUUID, condition);
            tabel.set("MissingBlocksToLevelUp", 0, condition);
            tabel.set("OneBlock_x", 0, condition);
            tabel.set("OneBlock_z", 0, condition);
            tabel.set("IslandSpawn_x", 0, condition);
            tabel.set("IslandSpawn_z", 0, condition);
            tabel.set("x_position", 0, condition);
            tabel.set("z_position", 0, condition);
            List<String> trustedList = new ArrayList<>();
            tabel.set("trusted", String.join(",", trustedList), condition);
            List<String> deniedList = new ArrayList<>();
            tabel.set("denied", String.join(",", deniedList), condition);

            List<String> invitedList = new ArrayList<>();
            tabel.set("invited", String.join(",", invitedList), condition);

        }
    }


    private static SQLTabel tabel;

    public DatenBankManager(Main pl) {
        HashMap<String, SQLDataType> columns = new HashMap<>();
        columns.put("owner", SQLDataType.CHAR);
        columns.put("WorldBorderSize", SQLDataType.INT);
        columns.put("TotalBlocks", SQLDataType.INT);
        columns.put("denied", SQLDataType.TEXT);
        columns.put("invited", SQLDataType.TEXT);
        columns.put("trusted", SQLDataType.TEXT);
        columns.put("owner_uuid", SQLDataType.CHAR);
        columns.put("EigeneInsel", SQLDataType.BOOLEAN);
        columns.put("MissingBlocksToLevelUp", SQLDataType.INT);
        columns.put("IslandLevel", SQLDataType.INT);
        columns.put("Durchgespielt", SQLDataType.BOOLEAN);
        columns.put("OneBlock_x", SQLDataType.INT);
        columns.put("OneBlock_z", SQLDataType.INT);
        columns.put("IslandSpawn_x", SQLDataType.INT);
        columns.put("IslandSpawn_z", SQLDataType.INT);
        columns.put("z_position", SQLDataType.INT);
        columns.put("x_position", SQLDataType.INT);
        tabel = new SQLTabel(pl.getConnection(), "userdata", columns);
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    public static void setInt(UUID uuid, String columnName, int value) {
        SQLTabel.Condition cond = new SQLTabel.Condition("owner_uuid", uuid.toString());
        tabel.set(columnName, value, cond);
    }

    public static void setString(UUID uuid, String columnName, String value) {
        SQLTabel.Condition cond = new SQLTabel.Condition("owner_uuid", uuid.toString());
        tabel.set(columnName, value, cond);
    }

    public static void setBoolean(UUID uuid, String columnName, boolean value) {
        SQLTabel.Condition cond = new SQLTabel.Condition("owner_uuid", uuid.toString());
        tabel.set(columnName, value, cond);
    }

    public static void setList(UUID uuid, String columnName, List<String> list) {
        SQLTabel.Condition cond = new SQLTabel.Condition("owner_uuid", uuid.toString());
        String csv = String.join(",", list);
        tabel.set(columnName, csv, cond);
    }

    public static String getString(UUID uuid, String columnName, String defaultValue) {
        SQLTabel.Condition condition = new SQLTabel.Condition("owner_uuid", uuid.toString());
        if (tabel.exits(condition)) {
            return tabel.getString(columnName, condition);
        }
        tabel.set(columnName, defaultValue, condition);
        return defaultValue;
    }

    public static int getInt(UUID uuid, String columnName, int defaultValue) {
        SQLTabel.Condition condition = new SQLTabel.Condition("owner_uuid", uuid.toString());
        if (tabel.exits(condition)) {
            return tabel.getInt(columnName, condition);
        }
        tabel.set(columnName, defaultValue, condition);
        return defaultValue;
    }

    public static boolean getBoolean(UUID uuid, String columnName, boolean defaultValue) {
        SQLTabel.Condition condition = new SQLTabel.Condition("owner_uuid", uuid.toString());
        if (tabel.exits(condition)) {
            return tabel.getBoolean(columnName, condition);
        }
        tabel.set(columnName, defaultValue, condition);
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
            return gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
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
    public static UUID getUUID(UUID userUUID, String key, UUID defaultValue) {
        try {
            String uuidString = getString(userUUID, key, null);
            if (uuidString != null && !uuidString.isEmpty()) {
                return UUID.fromString(uuidString);
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Ungültige UUID für " + key + " von " + userUUID + ": " + e.getMessage());
        }
        return defaultValue;
    }

    public static List<String> getStringList(UUID uuid, String column) {
        String listAsString = getString(uuid, column, ""); // Liste als CSV-String abrufen  (in komma umgewandlete Strings )
        if (listAsString.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(listAsString.split(",")); // CSV-String in Liste umwandeln  (in komma umgewandlete Strings )
    }

    public static void setStringList(UUID uuid, String column, List<String> values) {
        String listAsString = String.join(",", values); // Liste in CSV-String umwandeln (in komma umgewandlete Strings )
        setString(uuid, column, listAsString); //In die Datenbank
    }

    public static int getTotalBlocks() {
        int totalBlocks = -1;
        Connection connection = Main.getInstance().getConnection().getConnection(); // Connection bleibt offen
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
