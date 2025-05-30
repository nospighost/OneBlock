package de.Main.OneBlock.database;


import de.Main.OneBlock.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class MoneyManager implements Listener {
    private Main pl;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerUUID = event.getPlayer().getUniqueId().toString();  // z.B. "3f1a2b4c-..."
        Player player = event.getPlayer();

        // Erstelle HashMap der Spalten mit Typen (muss genau zu deiner Tabellenstruktur passen)
        HashMap<String, SQLDataType> columns = new HashMap<>();
        columns.put("owner", SQLDataType.CHAR);
        columns.put("WorldBorderSize", SQLDataType.INT);
        columns.put("TotalBlocks", SQLDataType.INT);
        columns.put("trusted", SQLDataType.CHAR);
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


        SQLTabel userTable = new SQLTabel(pl.getConnection(), playerUUID, columns);


        userTable.createUserTable(UUID.fromString(playerUUID));


        SQLTabel.Condition condition = new SQLTabel.Condition("owner", player.getName());
        if (!userTable.exits(condition)) {
            player.sendMessage("Tabelle für Spieler wird erstellt.");

            HashMap<String, Object> values = new HashMap<>();
            values.put("owner", player.getName());
            values.put("WorldBorderSize", 50);
            values.put("TotalBlocks", 200);
            values.put("MissingBlocksToLevelUp", 200);
            values.put("trusted", playerUUID);
            values.put("EigeneInsel", false);
            values.put("IslandLevel", 1);
            values.put("Durchgespielt", false);
            values.put("OneBlock_x", 0);
            values.put("OneBlock_z", 0);
            values.put("IslandSpawn_x", 0);
            values.put("owner_uuid", playerUUID);
            values.put("IslandSpawn_z", 0);
            values.put("z_position", 0);
            values.put("x_position", 0);

            userTable.insert(values);
        } else {
            player.sendMessage("Tabelle existiert bereits.");
        }
    }



    private static SQLTabel tabel;

    public MoneyManager(Main pl) {
        this.pl = pl;  // Plugin-Instanz
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
