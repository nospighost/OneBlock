package org.GhostLibs.oneBlock.events;

import org.GhostLibs.oneBlock.model.IslandPhase;
import org.GhostLibs.oneBlock.model.PlayerIsland;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class OBBreakOneblock extends OBBaseEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Block block;
    private final IslandPhase islandPhase;
    private PlayerIsland targetIsland;
    private Material nextMaterial;

    public OBBreakOneblock(Player player, PlayerIsland playerIsland, IslandPhase islandPhase, Block block, Material nextMaterial) {
        this.player = player;
        this.targetIsland = playerIsland;
        this.islandPhase = islandPhase;
        this.block = block;
        this.nextMaterial = nextMaterial;
    }

    public Player getPlayer() {
        return player;
    }

    public Block getBlock() {
        return block;
    }

    public IslandPhase getIslandPhase() {
        return islandPhase;
    }

    public PlayerIsland getTargetIsland() {
        return targetIsland;
    }

    public void setTargetIsland(PlayerIsland targetIsland) {
        this.targetIsland = targetIsland;
    }

    public Material getNextMaterial() {
        return nextMaterial;
    }

    public void setNextMaterial(Material nextMaterial) {
        if (nextMaterial != null && nextMaterial.isBlock()) {
            this.nextMaterial = nextMaterial;
        }
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}