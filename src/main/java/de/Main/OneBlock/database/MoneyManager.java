package de.Main.OneBlock.database;


import de.Main.OneBlock.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class MoneyManager implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerUUID = event.getPlayer().getUniqueId().toString();
        SQLTabel.Condition condition = new SQLTabel.Condition("owner", playerUUID);

        if (!islandTable.exits(condition)) {
            islandTable.set("owner", playerUUID, condition);
            islandTable.set("WorldBorderSize", 50, condition);
            islandTable.set("TotalBlocks", 0, condition);
            islandTable.set("EigeneInsel", true, condition);
            islandTable.set("IslandLevel", 1, condition);
            islandTable.set("Durchgespielt", false, condition);
            // ... weitere Standardwerte setzen
        }
    }



    private static SQLTabel tabel;

    public MoneyManager(Main pl) {
        HashMap<String, SQLDataType> colums = new HashMap<>();
        colums.put("owner", SQLDataType.CHAR);
        colums.put("WorldBorderSize", SQLDataType.INT);
        colums.put("TotalBlocks", SQLDataType.INT);
        colums.put("trusted", SQLDataType.CHAR);
        colums.put("owner_uuid", SQLDataType.CHAR);
        colums.put("EigeneInsel", SQLDataType.BOOLEAN);
        colums.put("MissingBlocksToLevelUp", SQLDataType.INT);
        colums.put("IslandLevel", SQLDataType.INT);
        colums.put("Durchgespielt", SQLDataType.BOOLEAN);
        colums.put("OneBlock_x", SQLDataType.INT);
        colums.put("OneBlock_z", SQLDataType.INT);
        colums.put("IslandSpawn_x", SQLDataType.INT);
        colums.put("IslandSpawn_z", SQLDataType.INT);
        colums.put("z_position", SQLDataType.INT);
        colums.put("x_position", SQLDataType.INT);
        tabel = new SQLTabel(pl.getConnection(), "userdata", colums);
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    public static void setInt(UUID uuid, int value) {
        SQLTabel.Condition condition = new SQLTabel.Condition("uuid", uuid.toString());
        if (tabel.exits(condition)) {
            tabel.set("value", value, condition);
        } else {
            tabel.set("uuid", uuid.toString(), condition);
            tabel.set("value", value, condition);
        }
    }

    public static void setString(String value, UUID uuid) {
        SQLTabel.Condition condition = new SQLTabel.Condition("uuid", uuid.toString());
        if (tabel.exits(condition)) {
            tabel.set("value", value, condition);
        } else {
            tabel.set("uuid", uuid.toString(), condition);
            tabel.set("value", value, condition);
        }
    }


    public static void setBoolean(String value, Boolean boolValue) {

        SQLTabel.Condition condition = new SQLTabel.Condition("uuid", value);

        if (tabel.exits(condition)) {

            tabel.set("value", boolValue, condition);
        } else {

            tabel.set("uuid", value, condition);
            tabel.set("value", boolValue, condition);
        }
    }


    public static int get(UUID uuid) {
        SQLTabel.Condition condition = new SQLTabel.Condition("uuid", uuid.toString());
        if (tabel.exits(condition))
            return tabel.getInt("value", condition);
        setInt(uuid, 0);
        return 0;
    }

}
