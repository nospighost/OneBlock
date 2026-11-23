package org.GhostLibs.oneBlock.manager;

import de.main.database.db.DBM;
import org.GhostLibs.oneBlock.model.PlayerData;
import org.GhostLibs.oneBlock.model.PlayerIsland;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OBManager {
    private final DBM dbm;

    public OBManager(DBM dbm) {
        this.dbm = dbm;
    }

    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        return dbm.getRow("player_data", "uuid", uuid.toString()).thenApply(row -> {
            if (row == null || row.isEmpty()) {
                return null;
            }
            return mapRowToPlayerData(row);
        });
    }

    public CompletableFuture<PlayerData> getPlayerData(int playerId) {
        return dbm.getRow("player_data", "player_id", playerId).thenApply(row -> {
            if (row == null || row.isEmpty()) {
                return null;
            }
            return mapRowToPlayerData(row);
        });
    }

    public CompletableFuture<Integer> getPlayerID(UUID uuid) {
        return dbm.getInt("player_data", "player_id", "uuid", uuid.toString(), -1);
    }


    public CompletableFuture<PlayerIsland> getPlayerIsland(UUID uuid) {
        return getPlayerID(uuid).thenCompose(id -> {
            if (id == -1) {
                return CompletableFuture.completedFuture(null);
            }
            return dbm.getRow("player_island", "owner_id", id).thenApply(row -> {
                if (row == null || row.isEmpty()) {
                    return null;
                }
                return mapRowToIsland(row);
            });
        });
    }

    public CompletableFuture<List<PlayerIsland>> getMembersIslands() {
        return dbm.selectAll("player_island").thenApply(rows -> {
            List<PlayerIsland> islands = new ArrayList<>();
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    islands.add(mapRowToIsland(row));
                }
            }
            return islands;
        });
    }


    private PlayerData mapRowToPlayerData(Map<String, Object> row) {
        int playerId = ((Number) row.get("player_id")).intValue();
        UUID uuid = UUID.fromString((String) row.get("uuid"));
        int islandResets = row.get("island_resets") != null ? ((Number) row.get("island_resets")).intValue() : 0;
        long lastSeen = row.get("last_seen") != null ? ((Number) row.get("last_seen")).longValue() : 0L;

        return new PlayerData(playerId, uuid, islandResets, lastSeen);
    }

    private PlayerIsland mapRowToIsland(Map<String, Object> row) {
        int islandId = ((Number) row.get("island_id")).intValue();
        int ownerId = ((Number) row.get("owner_id")).intValue();
        String islandName = (String) row.get("island_name");
        Location spawnLocation = (Location) row.get("spawn_location");
        Location oneblockLocation = (Location) row.get("oneblock_location");
        int borderSize = ((Number) row.get("border_size")).intValue();
        double islandLevel = ((Number) row.get("island_level")).doubleValue();
        String biome = (String) row.get("biome");
        boolean locked = (Boolean) row.get("locked");
        long createdAt = row.get("created_at") != null ? ((Number) row.get("created_at")).longValue() : 0L;

        return new PlayerIsland(
                islandId,
                ownerId,
                islandName,
                spawnLocation,
                oneblockLocation,
                borderSize,
                islandLevel,
                biome,
                locked,
                createdAt
        );
    }
}
