package org.GhostLibs.oneBlock.events;

import org.GhostLibs.oneBlock.model.PlayerData;
import org.GhostLibs.oneBlock.model.PlayerIsland;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class OBCreateIslandPhase extends OBBaseEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private PlayerData playerData;
    private PlayerIsland playerIsland;
    private Map<String, Object> phaseData;

    public OBCreateIslandPhase(Player player, PlayerData playerData, PlayerIsland playerIsland, Map<String, Object> phaseData) {
        this.player = player;
        this.playerData = playerData;
        this.playerIsland = playerIsland;
        this.phaseData = phaseData;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public void setPlayerData(PlayerData playerData) {
        this.playerData = playerData;
    }

    public PlayerIsland getPlayerIsland() {
        return playerIsland;
    }

    public void setPlayerIsland(PlayerIsland playerIsland) {
        this.playerIsland = playerIsland;
    }

    public Map<String, Object> getPhaseData() {
        return phaseData;
    }

    public void setPhaseData(Map<String, Object> phaseData) {
        this.phaseData = phaseData;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}