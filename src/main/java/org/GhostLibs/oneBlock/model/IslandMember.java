package org.GhostLibs.oneBlock.model;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class IslandMember {

    private int memberId;
    private int islandId;
    private int playerId;
    private UUID uuid;
    private String role = "MEMBER";
    private long joinedAt;
    private final Set<OBPermission> permissions = EnumSet.noneOf(OBPermission.class);

    public IslandMember() {
    }

    public IslandMember(int islandId, int playerId, String role, long joinedAt) {
        this.islandId = islandId;
        this.playerId = playerId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public static String serializePermissions(Set<OBPermission> permissions) {
        StringBuilder builder = new StringBuilder();
        for (OBPermission permission : permissions) {
            if (builder.length() > 0) builder.append(',');
            builder.append(permission.name());
        }
        return builder.toString();
    }

    public void loadPermissions(String serialized) {
        permissions.clear();
        if (serialized == null || serialized.isBlank()) return;
        for (String name : serialized.split(",")) {
            try {
                permissions.add(OBPermission.valueOf(name.trim()));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public String permissionsToString() {
        return serializePermissions(permissions);
    }

    public boolean hasPermission(OBPermission permission) {
        return permissions.contains(permission);
    }

    public void setPermission(OBPermission permission, boolean allowed) {
        if (allowed) {
            permissions.add(permission);
        } else {
            permissions.remove(permission);
        }
    }

    public Set<OBPermission> getPermissions() {
        return permissions;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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
                "memberId=" + memberId +
                ", islandId=" + islandId +
                ", playerId=" + playerId +
                ", role='" + role + '\'' +
                ", permissions=" + permissions +
                ", joinedAt=" + joinedAt +
                '}';
    }
}
