package org.GhostLibs.oneBlock.model;

import java.util.Objects;

public class IslandBan {

    private int islandId;
    private int bannedPlayerId;
    private String reason;

    public IslandBan() {
    }

    public IslandBan(int islandId, int bannedPlayerId, String reason) {
        this.islandId = islandId;
        this.bannedPlayerId = bannedPlayerId;
        this.reason = reason;
    }

    public int getIslandId() {
        return islandId;
    }

    public void setIslandId(int islandId) {
        this.islandId = islandId;
    }

    public int getBannedPlayerId() {
        return bannedPlayerId;
    }

    public void setBannedPlayerId(int bannedPlayerId) {
        this.bannedPlayerId = bannedPlayerId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IslandBan islandBan)) return false;
        return islandId == islandBan.islandId && bannedPlayerId == islandBan.bannedPlayerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(islandId, bannedPlayerId);
    }

    @Override
    public String toString() {
        return "IslandBan{" +
                "islandId=" + islandId +
                ", bannedPlayerId=" + bannedPlayerId +
                ", reason='" + reason + '\'' +
                '}';
    }
}
