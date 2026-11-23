package org.GhostLibs.oneBlock;

import de.main.database.db.DBM;
import de.main.database.db.ReferentialAction;
import de.main.database.db.SQLDataType;
import de.main.database.db.Table;

public final class DatabaseSchema {

    private final DBM dbm;

    public DatabaseSchema(DBM dbm) {
        this.dbm = dbm;
    }

    public void create() {
        dbm.createTables(
                layouts(),
                players(),
                playerSettings(),
                crates(),
                history(),
                playerCrates(),
                playerCrateStatistics()
        );
    }

    private Table layouts() {
        return Table.create("layouts")
                .column("id", SQLDataType.INT).autoIncrement().notNull().primaryKey()
                .column("layoutname", SQLDataType.VARCHAR_255)
                .column("sortindex", SQLDataType.INT)
                .column("items", SQLDataType.LONGTEXT);
    }

    private Table players() {
        return Table.create("players")
                .column("player_id", SQLDataType.INT).autoIncrement().notNull().primaryKey()
                .column("uuid", SQLDataType.VARCHAR_255).notNull().unique()
                .column("last_jackpot", SQLDataType.INT).nullable().defaultValue(0)
                .column("last_megajackpot", SQLDataType.INT).nullable().defaultValue(0);
    }

    private Table playerSettings() {
        return Table.create("playersettings")
                .column("player_id", SQLDataType.INT).notNull().primaryKey()
                .column("jackpot_sound", SQLDataType.BOOLEAN).defaultValue(true)
                .column("skip_animation", SQLDataType.BOOLEAN).defaultValue(false)
                .column("opening_sound", SQLDataType.VARCHAR_64).defaultValue("BLOCK_NOTE_BLOCK_PLING")
                .foreignKey("player_id").references("players", "player_id").onDelete(ReferentialAction.CASCADE);
    }

    private Table crates() {
        return Table.create("crates")
                .column("crate_id", SQLDataType.INT).autoIncrement().notNull().primaryKey()
                .column("casename", SQLDataType.VARCHAR_255)
                .column("layout", SQLDataType.INT).nullable()
                .column("pendingLayout", SQLDataType.INT).nullable()
                .column("cost", SQLDataType.INT)
                .column("location", SQLDataType.TEXT)
                .column("layoutswitch", SQLDataType.BIGINT)
                .column("switchRecurrence", SQLDataType.VARCHAR_255)
                .column("enabled", SQLDataType.BOOLEAN)
                .column("animation", SQLDataType.VARCHAR_255)
                .foreignKey("layout").references("layouts", "id").onDelete(ReferentialAction.SET_NULL)
                .foreignKey("pendingLayout").references("layouts", "id").onDelete(ReferentialAction.SET_NULL);
    }

    private Table history() {
        return Table.create("history")
                .column("id", SQLDataType.INT).autoIncrement().notNull().primaryKey()
                .column("player_id", SQLDataType.INT).notNull()
                .column("case_name", SQLDataType.VARCHAR_255)
                .column("rarity", SQLDataType.VARCHAR_255)
                .column("pull_time", SQLDataType.TIMESTAMP).defaultNow()
                .column("item", SQLDataType.LONGTEXT)
                .foreignKey("player_id").references("players", "player_id").onDelete(ReferentialAction.CASCADE)
                .index("player_id", "pull_time");
    }

    private Table playerCrates() {
        return Table.create("player_crates")
                .column("player_id", SQLDataType.INT).notNull().primaryKey()
                .column("crate_id", SQLDataType.INT).notNull().primaryKey()
                .column("amount", SQLDataType.INT).notNull().defaultValue(0)
                .foreignKey("player_id").references("players", "player_id").onDelete(ReferentialAction.CASCADE)
                .foreignKey("crate_id").references("crates", "crate_id").onDelete(ReferentialAction.CASCADE);
    }

    private Table playerCrateStatistics() {
        return Table.create("player_crate_statistics")
                .column("player_id", SQLDataType.INT).notNull().primaryKey()
                .column("crate_id", SQLDataType.INT).notNull().primaryKey()
                .column("opened", SQLDataType.INT).notNull().defaultValue(0)
                .foreignKey("player_id").references("players", "player_id").onDelete(ReferentialAction.CASCADE)
                .foreignKey("crate_id").references("crates", "crate_id").onDelete(ReferentialAction.CASCADE);
    }
}