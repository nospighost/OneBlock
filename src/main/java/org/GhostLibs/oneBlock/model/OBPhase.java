package org.GhostLibs.oneBlock.model;

import org.GhostLibs.oneBlock.manager.PhaseManager;
import org.bukkit.Material;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class OBPhase {

    private final int id;
    private final String name;
    private final int blockAmount;
    private final List<Material> blocks;

    public OBPhase(int id, String name, int blockAmount, List<Material> blocks) {
        this.id = id;
        this.name = name;
        this.blockAmount = blockAmount;
        this.blocks = blocks;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getBlockAmount() { return blockAmount; }
    public List<Material> getBlocks() { return blocks; }


    public boolean isHighestPhase() {
        return this.id >= PhaseManager.getHighestPhaseId();
    }

    public Material getRandomMaterial() {
        if (blocks == null || blocks.isEmpty()) {
            return Material.DIRT;
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(blocks.size());
        return blocks.get(randomIndex);
    }
}
