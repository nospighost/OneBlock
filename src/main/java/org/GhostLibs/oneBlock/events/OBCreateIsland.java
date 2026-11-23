package org.GhostLibs.oneBlock.events;

import org.GhostLibs.oneBlock.model.PlayerData;
import org.GhostLibs.oneBlock.model.PlayerIsland;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class OBCreateIsland extends OBBaseEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private PlayerData playerData;
    private PlayerIsland playerIsland; // COULD BE NULL
    private Map<String, Object> islandData;

    public OBCreateIsland(Player player, PlayerData playerData, Map<String, Object> islandData) {
        this.player = player;
        this.playerData = playerData;
        this.islandData = islandData;
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

    public Map<String, Object> getIslandData() {
        return islandData;
    }

    public void setIslandData(Map<String, Object> islandData) {
        this.islandData = islandData;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}