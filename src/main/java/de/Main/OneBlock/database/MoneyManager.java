package de.Main.OneBlock.database;


import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import de.Main.OneBlock.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MoneyManager implements Listener {
    private static final Gson gson = new Gson();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerUUID = event.getPlayer().getUniqueId().toString();
        SQLTabel.Condition condition = new SQLTabel.Condition("owner", playerUUID);

        if (!tabel.exits(condition)) {
            tabel.set("owner", playerUUID, condition);
            tabel.set("WorldBorderSize", 50, condition);
            tabel.set("TotalBlocks", 0, condition);
            tabel.set("EigeneInsel", true, condition);
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

    public MoneyManager(Main pl) {
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

    public static List<String> getList(UUID uuid, String columnName, List<String> defaultValue) {
        SQLTabel.Condition condition = new SQLTabel.Condition("owner_uuid", uuid.toString());
        if (tabel.exits(condition)) {
            String json = tabel.getString(columnName, condition);
            if (json != null && !json.isEmpty()) {
                return gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
            }
        }
        tabel.set(columnName, gson.toJson(defaultValue), condition);
        return defaultValue;
    }


}
