package org.GhostLibs.oneBlock.model;

import java.util.Objects;
import java.util.UUID;

public class PlayerData {

    private int playerId;
    private UUID uuid;
    private int islandResets;
    private long lastSeen;

    public PlayerData() {
    }

    public PlayerData(int playerId, UUID uuid, int islandResets, long lastSeen) {
        this.playerId = playerId;
        this.uuid = uuid;
        this.islandResets = islandResets;
        this.lastSeen = lastSeen;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getIslandResets() {
        return islandResets;
    }

    public void setIslandResets(int islandResets) {
        this.islandResets = islandResets;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerData that)) return false;
        return playerId == that.playerId && Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, uuid);
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "playerId=" + playerId +
                ", uuid=" + uuid +
                ", islandResets=" + islandResets +
                ", lastSeen=" + lastSeen +
                '}';
    }
}
