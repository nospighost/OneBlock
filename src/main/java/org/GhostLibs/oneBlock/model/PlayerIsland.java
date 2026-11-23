package org.GhostLibs.oneBlock.model;

import org.bukkit.Location;

import java.util.Objects;

public class PlayerIsland {

    private int islandId;
    private int ownerId;
    private String islandName;
    private Location spawnLocation;
    private Location oneblockLocation;
    private int borderSize = 7;
    private double islandLevel = 0.0;
    private String biome = "PLAINS";
    private boolean locked = false;
    private long createdAt;

    public PlayerIsland() {
    }

    public PlayerIsland(int islandId, int ownerId, String islandName, Location spawnLocation,
                        Location oneblockLocation, int borderSize, double islandLevel,
                        String biome, boolean locked, long createdAt) {
        this.islandId = islandId;
        this.ownerId = ownerId;
        this.islandName = islandName;
        this.spawnLocation = spawnLocation;
        this.oneblockLocation = oneblockLocation;
        this.borderSize = borderSize;
        this.islandLevel = islandLevel;
        this.biome = biome;
        this.locked = locked;
        this.createdAt = createdAt;
    }

    public int getIslandId() {
        return islandId;
    }

    public void setIslandId(int islandId) {
        this.islandId = islandId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getIslandName() {
        return islandName;
    }

    public void setIslandName(String islandName) {
        this.islandName = islandName;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public Location getOneblockLocation() {
        return oneblockLocation;
    }

    public void setOneblockLocation(Location oneblockLocation) {
        this.oneblockLocation = oneblockLocation;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }

    public double getIslandLevel() {
        return islandLevel;
    }

    public void setIslandLevel(double islandLevel) {
        this.islandLevel = islandLevel;
    }

    public String getBiome() {
        return biome;
    }

    public void setBiome(String biome) {
        this.biome = biome;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerIsland that)) return false;
        return islandId == that.islandId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(islandId);
    }

    @Override
    public String toString() {
        return "PlayerIsland{" +
                "islandId=" + islandId +
                ", ownerId=" + ownerId +
                ", islandName='" + islandName + '\'' +
                ", borderSize=" + borderSize +
                ", islandLevel=" + islandLevel +
                ", biome='" + biome + '\'' +
                ", locked=" + locked +
                ", createdAt=" + createdAt +
                '}';
    }
}
