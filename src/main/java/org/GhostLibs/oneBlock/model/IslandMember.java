package org.GhostLibs.oneBlock.model;

import java.util.Objects;

public class IslandMember {

    private int islandId;
    private int playerId;
    private String role = "MEMBER";
    private long joinedAt;

    public IslandMember() {
    }

    public IslandMember(int islandId, int playerId, String role, long joinedAt) {
        this.islandId = islandId;
        this.playerId = playerId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public int getIslandId() {
        return islandId;
    }

    public void setIslandId(int islandId) {
        this.islandId = islandId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(long joinedAt) {
        this.joinedAt = joinedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IslandMember that)) return false;
        return islandId == that.islandId && playerId == that.playerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(islandId, playerId);
    }

    @Override
    public String toString() {
        return "IslandMember{" +
                "islandId=" + islandId +
                ", playerId=" + playerId +
                ", role='" + role + '\'' +
                ", joinedAt=" + joinedAt +
                '}';
    }
}
