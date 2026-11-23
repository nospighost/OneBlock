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
                players(),
                playerIsland(),
                islandPhases(),
                islandMembers(),
                islandSettings(),
                islandBans()
        );
    }

    private Table players() {
        return Table.create("player_data")
                .column("player_id", SQLDataType.INT).autoIncrement().notNull().primaryKey()
                .column("uuid", SQLDataType.VARCHAR_255).notNull().unique()
                .column("island_resets", SQLDataType.INT).defaultValue(0)
                .column("last_seen", SQLDataType.BIGINT);
    }

    private Table playerIsland() {
        return Table.create("player_island")
                .column("island_id", SQLDataType.INT).autoIncrement().notNull().primaryKey()
                .column("owner_id", SQLDataType.INT).notNull()
                .column("island_name", SQLDataType.VARCHAR_255)
                .column("spawn_location", SQLDataType.LOCATION)
                .column("oneblock_location", SQLDataType.LOCATION)
                .column("border_size", SQLDataType.INT).defaultValue(7)
                .column("island_level", SQLDataType.DOUBLE).defaultValue(0.0)
                .column("biome", SQLDataType.VARCHAR_255).defaultValue("PLAINS")
                .column("locked", SQLDataType.BOOLEAN).defaultValue(false)
                .column("created_at", SQLDataType.BIGINT)
                .foreignKey("owner_id").references("player_data", "player_id").onDelete(ReferentialAction.CASCADE);
    }

    private Table islandPhases() {
        return Table.create("island_phases")
                .column("island_id", SQLDataType.INT).notNull().primaryKey()
                .column("current_phase_id", SQLDataType.INT).defaultValue(1)
                .column("selected_phase_id", SQLDataType.INT).defaultValue(1)
                .column("blocks_left", SQLDataType.INT).notNull()
                .column("mined_blocks", SQLDataType.BIGINT).defaultValue(0)
                .foreignKey("island_id").references("player_island", "island_id").onDelete(ReferentialAction.CASCADE);
    }

    private Table islandMembers() {
        return Table.create("island_members")
                .column("island_id", SQLDataType.INT).notNull()
                .column("player_id", SQLDataType.INT).notNull()
                .column("role", SQLDataType.VARCHAR_255).defaultValue("MEMBER")
                .column("joined_at", SQLDataType.BIGINT)
                .foreignKey("island_id").references("player_island", "island_id").onDelete(ReferentialAction.CASCADE)
                .foreignKey("player_id").references("player_data", "player_id").onDelete(ReferentialAction.CASCADE);
    }

    private Table islandSettings() {
        return Table.create("island_settings")
                .column("island_id", SQLDataType.INT).notNull().primaryKey()
                .column("pvp_enabled", SQLDataType.BOOLEAN).defaultValue(false)
                .column("mob_spawning", SQLDataType.BOOLEAN).defaultValue(true)
                .column("visitor_interact", SQLDataType.BOOLEAN).defaultValue(false)
                .column("fire_spread", SQLDataType.BOOLEAN).defaultValue(false)
                .foreignKey("island_id").references("player_island", "island_id").onDelete(ReferentialAction.CASCADE);
    }

    private Table islandBans() {
        return Table.create("island_bans")
                .column("island_id", SQLDataType.INT).notNull()
                .column("banned_player_id", SQLDataType.INT).notNull()
                .column("reason", SQLDataType.VARCHAR_255)
                .foreignKey("island_id").references("player_island", "island_id").onDelete(ReferentialAction.CASCADE)
                .foreignKey("banned_player_id").references("player_data", "player_id").onDelete(ReferentialAction.CASCADE);
    }
}
