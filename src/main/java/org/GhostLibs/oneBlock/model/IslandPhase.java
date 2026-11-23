package org.GhostLibs.oneBlock.model;

import java.util.Objects;

public class IslandPhase {

    private int islandId;
    private int currentPhaseId = 1;
    private int selectedPhaseId = 1;
    private int blocksLeft;
    private long minedBlocks = 0;

    public IslandPhase() {
    }

    public IslandPhase(int islandId, int currentPhaseId, int selectedPhaseId, int blocksLeft, long minedBlocks) {
        this.islandId = islandId;
        this.currentPhaseId = currentPhaseId;
        this.selectedPhaseId = selectedPhaseId;
        this.blocksLeft = blocksLeft;
        this.minedBlocks = minedBlocks;
    }

    public int getIslandId() {
        return islandId;
    }

    public void setIslandId(int islandId) {
        this.islandId = islandId;
    }

    public int getCurrentPhaseId() {
        return currentPhaseId;
    }

    public void setCurrentPhaseId(int currentPhaseId) {
        this.currentPhaseId = currentPhaseId;
    }

    public int getSelectedPhaseId() {
        return selectedPhaseId;
    }

    public void setSelectedPhaseId(int selectedPhaseId) {
        this.selectedPhaseId = selectedPhaseId;
    }

    public int getBlocksLeft() {
        return blocksLeft;
    }

    public void setBlocksLeft(int blocksLeft) {
        this.blocksLeft = blocksLeft;
    }

    public long getMinedBlocks() {
        return minedBlocks;
    }

    public void setMinedBlocks(long minedBlocks) {
        this.minedBlocks = minedBlocks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IslandPhase that)) return false;
        return islandId == that.islandId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(islandId);
    }

    @Override
    public String toString() {
        return "IslandPhase{" +
                "islandId=" + islandId +
                ", currentPhaseId=" + currentPhaseId +
                ", selectedPhaseId=" + selectedPhaseId +
                ", blocksLeft=" + blocksLeft +
                ", minedBlocks=" + minedBlocks +
                '}';
    }
}
