package org.GhostLibs.oneBlock.manager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.main.database.db.DBM;
import de.main.database.util.MessageUtil;
import org.GhostLibs.oneBlock.Sprache;
import org.GhostLibs.oneBlock.events.OBCreateIsland;
import org.GhostLibs.oneBlock.events.OBCreateIslandPhase;
import org.GhostLibs.oneBlock.events.OBCreateIslandSettings;
import org.GhostLibs.oneBlock.model.IslandMember;
import org.GhostLibs.oneBlock.model.IslandPhase;
import org.GhostLibs.oneBlock.model.MemberResult;
import org.GhostLibs.oneBlock.model.OBPermission;
import org.GhostLibs.oneBlock.model.PlayerData;
import org.GhostLibs.oneBlock.model.PlayerIsland;
import org.GhostLibs.oneBlock.util.OBEventUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class OBManager {
    private static final int START_BORDER_SIZE = 7;
    private static OBManager instance;
    private final DBM dbm;
    private final JavaPlugin plugin;
    private final WorldManager worldManager;
    private final Set<UUID> busy = ConcurrentHashMap.newKeySet();
    private volatile IslandCache islandCache;

    public static OBManager getInstance() {
        return instance;
    }

    public OBManager(DBM dbm, JavaPlugin plugin, WorldManager worldManager) {
        this.dbm = dbm;
        instance = this;
        this.plugin = plugin;
        this.worldManager = worldManager;
    }

    public void setIslandCache(IslandCache islandCache) {
        this.islandCache = islandCache;
    }

    private static final class IslandAbort extends RuntimeException {
        IslandAbort() {
            super(null, null, false, false);
        }
    }

    private static <T> CompletableFuture<T> abort() {
        return CompletableFuture.failedFuture(new IslandAbort());
    }

    private static boolean isAbort(Throwable t) {
        while (t != null) {
            if (t instanceof IslandAbort) return true;
            t = t.getCause();
        }
        return false;
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

    public void createPlayerData(Map<String, Object> data) {
        dbm.exists("player_data", "uuid", data.get("uuid")).thenAccept(exists -> {
            if (exists) {
                return;
            }
            dbm.insert("player_data", data);
        });
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

    public CompletableFuture<PlayerIsland> getPlayerIslandByPlayerId(int playerId) {
        if (playerId == -1) {
            return CompletableFuture.completedFuture(null);
        }

        return dbm.getRow("player_island", "owner_id", playerId).thenApply(row -> {
            if (row == null || row.isEmpty()) {
                return null;
            }
            return mapRowToIsland(row, null);
        });
    }

    public CompletableFuture<List<PlayerIsland>> getMembersIslands(UUID uuid) {
        return getPlayerID(uuid).thenCompose(playerId -> {
            if (playerId == -1) {
                return CompletableFuture.completedFuture(List.<PlayerIsland>of());
            }

            // TODO: durch eine WHERE-Abfrage ersetzen, falls der DBM eine Mehrfach-Zeilen-Abfrage anbietet
            return dbm.selectAll("island_members").thenCompose(rows -> {
                List<CompletableFuture<PlayerIsland>> futures = new ArrayList<>();
                if (rows != null) {
                    for (Map<String, Object> row : rows) {
                        if (((Number) row.get("player_id")).intValue() != playerId) continue;

                        int islandId = ((Number) row.get("island_id")).intValue();
                        futures.add(getPlayerIsland(islandId).thenCompose(island -> {
                            if (island == null) {
                                return CompletableFuture.<PlayerIsland>completedFuture(null);
                            }

                            return getPlayerData(island.getOwnerId()).thenApply(ownerData -> {
                                if (ownerData == null) {
                                    return null;
                                }
                                island.setOwnerUUID(ownerData.getUuid());
                                return island;
                            });
                        }));
                    }
                }

                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> futures.stream()
                                .map(CompletableFuture::join)
                                .filter(Objects::nonNull)
                                .toList());
            });
        });
    }

    public CompletionStage<IslandPhase> getIslandPhase(int islandId) {
            return dbm.getRow("island_phases", "island_id", islandId).thenApply(row -> {
                if (row == null || row.isEmpty()) {
                    return null;
                }
                return mapRowToIslandPhase(row);
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
        Location spawnLocation = toLocation(row.get("spawn_location"));
        Location oneblockLocation = toLocation(row.get("oneblock_location"));
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
                uuid,
                spawnLocation.getWorld(),
                List.of()
        );
    }

    private IslandPhase mapRowToIslandPhase(Map<String, Object> row) {

        int phaseId = ((Number) row.get("current_phase_id")).intValue();
        int selectedPhaseId = ((Number) row.get("selected_phase_id")).intValue();
        int blocksLeft = ((Number) row.get("blocks_left")).intValue();
        int minedBlocks = ((Number) row.get("mined_blocks")).intValue();
        int islandId = ((Number) row.get("island_id")).intValue();

        return new IslandPhase(
                islandId,
                phaseId,
                selectedPhaseId,
                blocksLeft,
                minedBlocks
        );
    }

    private CompletableFuture<IslandMember> mapRowToIslandMember(Map<String, Object> row) {
        int playerId = ((Number) row.get("player_id")).intValue();

        return getPlayerData(playerId).thenApply(playerData -> {
            if (playerData == null) {
                return null;
            }

            IslandMember member = new IslandMember();
            member.setMemberId(((Number) row.get("member_id")).intValue());
            member.setIslandId(((Number) row.get("island_id")).intValue());
            member.setPlayerId(playerId);
            member.setUuid(playerData.getUuid());
            member.setRole(row.get("role") != null ? (String) row.get("role") : "MEMBER");
            member.setJoinedAt(row.get("joined_at") != null ? ((Number) row.get("joined_at")).longValue() : 0L);
            member.loadPermissions((String) row.get("permissions"));
            return member;
        });
    }

    private Location toLocation(Object value) {
        if (value == null) return null;
        if (value instanceof Location loc) return loc;

        JsonObject json = JsonParser.parseString(value.toString()).getAsJsonObject();

        String worldName = json.has("world") && !json.get("world").isJsonNull()
                ? json.get("world").getAsString()
                : null;
        World world = worldName != null ? Bukkit.getWorld(worldName) : null;

        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        float yaw = json.has("yaw") ? json.get("yaw").getAsFloat() : 0f;
        float pitch = json.has("pitch") ? json.get("pitch").getAsFloat() : 0f;

        return new Location(world, x, y, z, yaw, pitch);
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

         // TODO: Gucken ob andere werte außer Biome auch überschrieben werden wenn ich diese im Event ändere
        Map<String, Object> values = new HashMap<>();
        values.put("biome", island.getBiome());

        return DBM.get().updateRow("player_island", values, "island_id", island.getIslandId());
    }

    public CompletableFuture<Boolean> updateIslandPhase(IslandPhase phase) {
        if (phase == null) {
            return CompletableFuture.completedFuture(false);
        }

        Map<String, Object> values = new HashMap<>();
        values.put("current_phase_id", phase.getCurrentPhaseId());
        values.put("selected_phase_id", phase.getSelectedPhaseId());
        values.put("blocks_left", phase.getBlocksLeft());
        values.put("mined_blocks", phase.getMinedBlocks());

        return dbm.updateRow("island_phases", values, "island_id", phase.getIslandId());
    }

    public CompletableFuture<Void> createIsland(Player player, int playerId, PlayerData playerData) {
        if (playerData == null) {
            sendMessageSync(player, Sprache.get("ERRORS.NO_PLAYER_DATA", "%player%", player.getName()));
            return CompletableFuture.completedFuture(null);
        }

        UUID uuid = player.getUniqueId();
        if (!busy.add(uuid)) {
            sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_BUSY"));
            return CompletableFuture.completedFuture(null);
        }

        return getPlayerIsland(uuid)
                .thenCompose(existing -> {
                    if (existing != null) {
                        sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_ALREADY_EXISTS"));
                        return CompletableFuture.<Void>completedFuture(null);
                    }

                    return worldManager.createWorld(player, START_BORDER_SIZE)
                            .exceptionallyCompose(ex -> rollback(uuid)
                                    .thenCompose(v -> CompletableFuture.<World>failedFuture(ex)))
                            .thenCompose(world -> buildIsland(player, playerId, playerData, world)
                                    .handle((ok, ex) -> {
                                        if (ex == null) {
                                            teleportToIsland(player, world);
                                            return CompletableFuture.<Void>completedFuture(null);
                                        }
                                        return rollback(uuid).thenCompose(v -> isAbort(ex)
                                                ? CompletableFuture.<Void>completedFuture(null)
                                                : CompletableFuture.<Void>failedFuture(ex));
                                    })
                                    .thenCompose(f -> f));
                })
                .whenComplete((v, ex) -> busy.remove(uuid));
    }

    private CompletableFuture<Void> buildIsland(Player player, int playerId, PlayerData playerData, World world) {
        Map<String, Object> islandData = new HashMap<>();
        islandData.put("owner_id", playerId);
        islandData.put("island_name", player.getName() + "'s Island");
        islandData.put("spawn_location", new Location(world, 0.5, 101, 0.5));
        islandData.put("oneblock_location", new Location(world, 0, 100, 0));
        islandData.put("border_size", START_BORDER_SIZE);
        islandData.put("island_level", 0.0);
        islandData.put("biome", "PLAINS");
        islandData.put("locked", false);
        islandData.put("created_at", System.currentTimeMillis());

        OBCreateIsland event = new OBCreateIsland(player, playerData, islandData);

        return OBEventUtil.callSync(plugin, event).thenCompose(createEvent -> {
            if (createEvent.isCancelled()) {
                return abort();
            }

            return createPlayerIsland(createEvent.getIslandData()).thenCompose(islandId -> {
                if (islandId == -1) {
                    sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_ERROR_CREATED"));
                    return abort();
                }

                if (islandId == -2) {
                    sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_ALREADY_EXISTS"));
                    return abort();
                }

                return loadIsland(player, islandId, createEvent);
            });
        });
    }

    private CompletableFuture<Void> loadIsland(Player player, int islandId, OBCreateIsland createEvent) {
        return getPlayerIsland(islandId)
                .thenCompose(playerIsland -> {
                    if (playerIsland == null) {
                        return CompletableFuture.<Void>failedFuture(new IllegalStateException("PlayerIsland could not be loaded."));
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
                        return abort();
                    }

                    PlayerIsland modifiedIsland = settingsEvent.getPlayerIsland();

                    if (modifiedIsland == null) {
                        return CompletableFuture.<Void>failedFuture(
                                new IllegalStateException("PlayerIsland cannot be null.")
                        );
                    }

                    createEvent.setPlayerIsland(modifiedIsland);

                    return updatePlayerIsland(modifiedIsland)
                            .thenCompose(updated -> {
                                if (!updated) {
                                    return CompletableFuture.<Void>failedFuture(
                                            new IllegalStateException("Could not update PlayerIsland.")
                                    );
                                }

                                return createIslandSettings(settingsEvent.getSettingsData())
                                        .thenCompose(settingsId -> {
                                            if (settingsId == null || settingsId <= 0) {
                                                sendMessageSync(player, Sprache.get("COMMANDS.ISLANDSETTINGS_ERROR_CREATED"));
                                                return CompletableFuture.<Void>failedFuture(new IslandAbort());
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
                        return abort();
                    }

                    return createIslandPhases(phaseEvent.getPhaseData())
                            .thenAccept(success -> {
                                if (success == null || success == -1) {
                                    sendMessageSync(player, Sprache.get("COMMANDS.ISLANDPHASES_ERROR_CREATED"));
                                    throw new IslandAbort();
                                }

                                sendMessageSync(player, Sprache.get("COMMANDS.ISLAND_SUCCESSFULL_CREATED"));
                            });
                });
    }

    private CompletableFuture<Void> rollback(UUID uuid) {
        return deleteIsland(uuid).toCompletableFuture()
                .handle((r, ex) -> (Void) null)
                .thenCompose(v -> worldManager.deleteWorld(uuid))
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, "Rollback fehlgeschlagen für " + uuid, ex);
                    return null;
                });
    }

    private void teleportToIsland(Player player, World world) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                player.teleportAsync(world.getSpawnLocation());
            }
        });
    }

    public CompletionStage<Integer> deleteIsland(UUID uuid) {
        return getPlayerID(uuid).thenCompose(playerId -> {
            if (playerId == -1) {
                return CompletableFuture.completedFuture(-1);
            }

            return dbm.exists("player_island", "owner_id", playerId).thenCompose(exists -> {
                if (!exists) {
                    return CompletableFuture.completedFuture(-3);
                }

                return dbm.delete("player_island", "owner_id", playerId)
                        .thenCompose(ignored -> worldManager.deleteWorld(uuid))
                        .thenApply(v -> 0)
                        .exceptionally(exception -> {
                            plugin.getLogger().log(Level.SEVERE, "deleteIsland fehlgeschlagen für " + uuid, exception);
                            return -2;
                        });
            });
        });
    }

    public void teleportToOneblock(int playerId, Player player) {
        getPlayerIslandByPlayerId(playerId).thenAccept(island -> {
            if (island == null || island.getOneblockLocation() == null) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                Location loc = island.getSpawnLocation();

                if (loc.getWorld() == null) return;
                String worldName = loc.getWorld().getName();

                World world = worldManager.loadWorld(worldName);
                if (world == null) return;

                loc.setWorld(world);

                player.teleportAsync(loc);
            });
        });
    }

    public CompletableFuture<List<IslandMember>> getIslandMembers(int islandId) {
        // TODO: durch eine WHERE-Abfrage ersetzen, falls der DBM eine Mehrfach-Zeilen-Abfrage anbietet
        return dbm.selectAll("island_members").thenCompose(rows -> {
            List<CompletableFuture<IslandMember>> futures = new ArrayList<>();
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    if (((Number) row.get("island_id")).intValue() != islandId) continue;
                    futures.add(mapRowToIslandMember(row));
                }
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)
                            .filter(Objects::nonNull)
                            .toList());
        });
    }

    /** Liefert das Mitglied der Insel des Besitzers oder null, falls es keine Insel / kein Mitglied gibt. */
    public CompletableFuture<IslandMember> getIslandMember(UUID ownerUUID, UUID memberUUID) {
        return getPlayerIsland(ownerUUID).thenCompose(island -> {
            if (island == null) {
                return CompletableFuture.<IslandMember>completedFuture(null);
            }

            return getPlayerID(memberUUID).thenCompose(memberPlayerId -> {
                if (memberPlayerId == -1) {
                    return CompletableFuture.<IslandMember>completedFuture(null);
                }

                return getIslandMembers(island.getIslandId()).thenApply(members -> members.stream()
                        .filter(member -> member.getPlayerId() == memberPlayerId)
                        .findFirst()
                        .orElse(null));
            });
        });
    }

    public CompletableFuture<MemberResult> addIslandMember(UUID ownerUUID, UUID targetUUID) {
        if (ownerUUID.equals(targetUUID)) {
            return CompletableFuture.completedFuture(MemberResult.IS_OWNER);
        }

        return getPlayerIsland(ownerUUID).thenCompose(island -> {
            if (island == null) {
                return CompletableFuture.completedFuture(MemberResult.NO_ISLAND);
            }

            return getPlayerID(targetUUID).thenCompose(targetPlayerId -> {
                if (targetPlayerId == -1) {
                    return CompletableFuture.completedFuture(MemberResult.NO_PLAYER_DATA);
                }

                return getIslandMembers(island.getIslandId()).thenCompose(members -> {
                    if (members.stream().anyMatch(member -> member.getPlayerId() == targetPlayerId)) {
                        return CompletableFuture.completedFuture(MemberResult.ALREADY_MEMBER);
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("island_id", island.getIslandId());
                    data.put("player_id", targetPlayerId);
                    data.put("role", "MEMBER");
                    data.put("permissions", "");
                    data.put("joined_at", System.currentTimeMillis());

                    return dbm.insert("island_members", data).thenCompose(result -> {
                        if (!Boolean.TRUE.equals(result)) {
                            return CompletableFuture.completedFuture(MemberResult.ERROR);
                        }

                        return reloadCachedMembers(ownerUUID).thenApply(v -> MemberResult.ADDED);
                    });
                });
            });
        });
    }

    public CompletableFuture<MemberResult> removeIslandMember(UUID ownerUUID, UUID targetUUID) {
        if (ownerUUID.equals(targetUUID)) {
            return CompletableFuture.completedFuture(MemberResult.IS_OWNER);
        }

        return getPlayerIsland(ownerUUID).thenCompose(island -> {
            if (island == null) {
                return CompletableFuture.completedFuture(MemberResult.NO_ISLAND);
            }

            return getIslandMember(ownerUUID, targetUUID).thenCompose(member -> {
                if (member == null) {
                    return CompletableFuture.completedFuture(MemberResult.NOT_MEMBER);
                }

                return dbm.delete("island_members", "member_id", member.getMemberId())
                        .thenCompose(ignored -> reloadCachedMembers(ownerUUID))
                        .thenApply(v -> MemberResult.REMOVED);
            });
        });
    }

    public CompletableFuture<Boolean> updateMemberPermissions(UUID ownerUUID, IslandMember member) {
        if (member == null) {
            return CompletableFuture.completedFuture(false);
        }

        Map<String, Object> values = new HashMap<>();
        values.put("permissions", member.permissionsToString());

        return dbm.updateRow("island_members", values, "member_id", member.getMemberId())
                .thenCompose(success -> {
                    if (!Boolean.TRUE.equals(success)) {
                        return CompletableFuture.completedFuture(false);
                    }
                    return reloadCachedMembers(ownerUUID).thenApply(v -> true);
                });
    }

    private CompletableFuture<Void> reloadCachedMembers(UUID ownerUUID) {
        IslandCache cache = islandCache;
        if (cache == null) {
            return CompletableFuture.completedFuture(null);
        }
        return cache.reloadMembers(ownerUUID);
    }

    public boolean hasPermission(UUID uuid, PlayerIsland island, OBPermission obPermission) {
        if (uuid == null) return false;
        if (island == null) return false;
        if (obPermission == null) return false;

        return island.hasPermission(uuid, obPermission);
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
}