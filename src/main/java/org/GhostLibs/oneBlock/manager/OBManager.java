package org.GhostLibs.oneBlock.manager;

import de.main.database.db.DBM;
import de.main.database.util.MessageUtil;
import org.GhostLibs.oneBlock.OneBlock;
import org.GhostLibs.oneBlock.Sprache;
import org.GhostLibs.oneBlock.events.OBCreateIsland;
import org.GhostLibs.oneBlock.events.OBCreateIslandPhase;
import org.GhostLibs.oneBlock.events.OBCreateIslandSettings;
import org.GhostLibs.oneBlock.model.PlayerData;
import org.GhostLibs.oneBlock.model.PlayerIsland;
import org.GhostLibs.oneBlock.util.OBEventUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class OBManager {
    private final DBM dbm;
    private static OBManager instance;
    private final JavaPlugin plugin;

    public static OBManager getInstance() {
        return instance;
    }
    public OBManager(DBM dbm, JavaPlugin plugin) {
        this.dbm = dbm;
        instance = this;
        this.plugin = plugin;
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
                return mapRowToIsland(row, uuid);
            });
        });
    }

    public CompletableFuture<PlayerIsland> getPlayerIsland(int islandID) {
        if (islandID == -1) {
            return CompletableFuture.completedFuture(null);
        }
        return dbm.getRow("player_island", "island_id", islandID).thenApply(row -> {
            if (row == null || row.isEmpty()) {
                return null;
            }
            return mapRowToIsland(row, null);
        });

    }

    public CompletableFuture<List<PlayerIsland>> getMembersIslands(UUID uuid) {
        return dbm.selectAll("player_island").thenApply(rows -> {
            List<PlayerIsland> islands = new ArrayList<>();
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    islands.add(mapRowToIsland(row, uuid));
                }
            }
            return islands;
        });
    }


    private PlayerData mapRowToPlayerData(Map<String, Object> row) {
        int playerId = ((Number) row.get("player_id")).intValue();
        UUID uuid = UUID.fromString((String) row.get("uuid"));
        int islandResets = row.get("island_resets") != null ? ((Number) row.get("island_resets")).intValue() : 0;
        Long lastSeen = row.get("last_seen") != null ? ((Long) row.get("last_seen")) : System.currentTimeMillis();

        return new PlayerData(playerId, uuid, islandResets, lastSeen);
    }

    private PlayerIsland mapRowToIsland(Map<String, Object> row, UUID uuid) {
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
                createdAt,
                uuid
        );
    }

    public void createPlayerData(Map<String, Object> data) {
        dbm.exists("player_data", "uuid", data.get("uuid")).thenAccept(exists -> {
            if (exists) {
                return;
            }
            dbm.insert("player_data", data);
        });
    }

    public CompletableFuture<Integer> createPlayerIsland(Map<String, Object> data) {
        Object ownerId = data.get("owner_id");

        if (ownerId == null) {
            return CompletableFuture.completedFuture(-1);
        }

        return dbm.exists("player_island", "owner_id", ownerId)
                .thenCompose(exists -> {
                    if (exists) {
                        return CompletableFuture.completedFuture(-2);
                    }

                    return dbm.insertGetKey("player_island", data)
                            .thenApply(result -> {
                                if (result == null) {
                                    return -1;
                                }

                                return ((Number) result).intValue();
                            });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return -1;
                });
    }

    public CompletableFuture<Integer> createIslandSettings(Map<String, Object> data) {
        Object islandId = data.get("island_id");

        if (islandId == null) {
            return CompletableFuture.completedFuture(-1);
        }

        return dbm.exists("island_settings", "island_id", islandId)
                .thenCompose(exists -> {
                    if (exists) {
                        return CompletableFuture.completedFuture(null);
                    }

                    return dbm.insert("island_settings", data)
                            .thenApply(result -> {
                                if (result == null) {
                                    return -1;
                                }

                                return 1;
                            });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return -1;
                });
    }

    public CompletableFuture<Integer> createIslandPhases(Map<String, Object> data) {
        Object islandId = data.get("island_id");

        if (islandId == null) {
            return CompletableFuture.completedFuture(-1);
        }

        return dbm.exists("island_phases", "island_id", islandId)
                .thenCompose(exists -> {
                    if (exists) {
                        return CompletableFuture.completedFuture(null);
                    }

                    return dbm.insert("island_phases", data)
                            .thenApply(result -> {
                                if (result == null) {
                                    return -1;
                                }

                                return ((boolean) result ? 0 : -1);
                            });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return -1;
                });
    }

    public CompletableFuture<Boolean> updatePlayerIsland(PlayerIsland island) {
        if (island == null) {
            return CompletableFuture.completedFuture(false);
        }

        Map<String, Object> values = new HashMap<>();
        values.put("biome", island.getBiome());

        return DBM.get().updateRow("player_island", values, "island_id", island.getIslandId());
    }

    public CompletableFuture<Void> createIsland(Player player, int playerId, PlayerData playerData) {
        if (playerData == null) {
            sendMessageSync(player, Sprache.get("ERRORS.NO_PLAYER_DATA", "%player%", player.getName()));
            return CompletableFuture.completedFuture(null);
        }

        Map<String, Object> islandData = new HashMap<>();
        islandData.put("owner_id", playerId);
        islandData.put("island_name", player.getName() + "'s Island");
        islandData.put("spawn_location", null);
        islandData.put("oneblock_location", null);
        islandData.put("border_size", 7);
        islandData.put("island_level", 0.0);
        islandData.put("biome", "PLAINS");
        islandData.put("locked", false);
        islandData.put("created_at", System.currentTimeMillis());

        OBCreateIsland event = new OBCreateIsland(player, playerData, islandData);

        return OBEventUtil.callSync(plugin, event)
                .thenCompose(createEvent -> {
                    if (createEvent.isCancelled()) {
                        return CompletableFuture.completedFuture(null);
                    }

                    return OBManager.getInstance().createPlayerIsland(createEvent.getIslandData())
                            .thenCompose(islandId -> {
                                if (islandId == -1) {
                                    sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_ERROR_CREATED"));
                                    return CompletableFuture.completedFuture(null);
                                }

                                if (islandId == -2) {
                                    sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_ALREADY_EXISTS"));
                                    return CompletableFuture.completedFuture(null);
                                }

                                return loadIsland(player, islandId, createEvent);
                            });
                });
    }

    private CompletableFuture<Void> loadIsland(Player player, int islandId, OBCreateIsland createEvent) {
        OBManager manager = OBManager.getInstance();

        return manager.getPlayerIsland(islandId)
                .thenCompose(playerIsland -> {
                    if (playerIsland == null) {
                        return CompletableFuture.failedFuture(new IllegalStateException("PlayerIsland could not be loaded."));
                    }

                    createEvent.setPlayerIsland(playerIsland);

                    return createIslandSettings(player, islandId, createEvent, playerIsland);
                });
    }

    private CompletableFuture<Void> createIslandSettings(Player player, int islandId, OBCreateIsland createEvent, PlayerIsland playerIsland) {
        Map<String, Object> settingsData = new HashMap<>();
        settingsData.put("island_id", islandId);
        settingsData.put("pvp_enabled", false);
        settingsData.put("mob_spawning", true);
        settingsData.put("visitor_interact", false);
        settingsData.put("fire_spread", false);

        OBCreateIslandSettings event = new OBCreateIslandSettings(
                player,
                createEvent.getPlayerData(),
                playerIsland,
                settingsData
        );

        return OBEventUtil.callSync(plugin, event)
                .thenCompose(settingsEvent -> {
                    if (settingsEvent.isCancelled()) {
                        return CompletableFuture.completedFuture(null);
                    }

                    PlayerIsland modifiedIsland = settingsEvent.getPlayerIsland();

                    if (modifiedIsland == null) {
                        return CompletableFuture.failedFuture(
                                new IllegalStateException("PlayerIsland cannot be null.")
                        );
                    }

                    createEvent.setPlayerIsland(modifiedIsland);

                    return OBManager.getInstance()
                            .updatePlayerIsland(modifiedIsland)
                            .thenCompose(updated -> {
                                if (!updated) {
                                    return CompletableFuture.failedFuture(
                                            new IllegalStateException("Could not update PlayerIsland.")
                                    );
                                }

                                return OBManager.getInstance()
                                        .createIslandSettings(settingsEvent.getSettingsData())
                                        .thenCompose(settingsId -> {
                                            if (settingsId <= 0) {
                                                sendMessageSync(player, Sprache.get("COMMANDS.ISLANDSETTINGS_ERROR_CREATED"));
                                                return CompletableFuture.completedFuture(null);
                                            }

                                            return createIslandPhase(player, settingsEvent);
                                        });
                            });
                });
    }

    private CompletableFuture<Void> createIslandPhase(Player player, OBCreateIslandSettings settingsEvent) {
        Map<String, Object> phaseData = new HashMap<>();
        phaseData.put("island_id", settingsEvent.getPlayerIsland().getIslandId());
        phaseData.put("current_phase_id", 1);
        phaseData.put("selected_phase_id", 1);
        phaseData.put("mined_blocks", 0);
        phaseData.put("blocks_left", PhaseManager.getPhase(1).getBlockAmount());

        OBCreateIslandPhase event = new OBCreateIslandPhase(
                player,
                settingsEvent.getPlayerData(),
                settingsEvent.getPlayerIsland(),
                phaseData
        );

        return OBEventUtil.callSync(plugin, event)
                .thenCompose(phaseEvent -> {
                    if (phaseEvent.isCancelled()) {
                        return CompletableFuture.completedFuture(null);
                    }

                    return OBManager.getInstance()
                            .createIslandPhases(phaseEvent.getPhaseData())
                            .thenAccept(success -> {
                                if (success == -1) {
                                    sendMessageSync(player, Sprache.get("COMMANDS.ISLANDPHASES_ERROR_CREATED"));
                                    return;
                                }

                                if (success == 0) {
                                    sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_SUCCESSFULL_CREATED"));
                                }
                            });
                });
    }

    public void sendMessageSync(Player player, String message) {
        Runnable task = () -> {
            if (player.isOnline()) {
                MessageUtil.send(player, message);
            }
        };

        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public CompletionStage<Integer> deleteIsland(UUID uuid) {
        return getPlayerID(uuid).thenCompose(playerId -> {
            if (playerId == -1) {
                return CompletableFuture.completedFuture(-1);
            }

            try {
                dbm.delete("player_island", "owner_id", playerId);
            } catch (Exception exception) {
                exception.printStackTrace();
                return CompletableFuture.completedFuture(-2);
            }

            return CompletableFuture.completedFuture(0);
        });
    }
}
