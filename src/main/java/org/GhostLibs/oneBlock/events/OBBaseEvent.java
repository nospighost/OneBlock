package org.GhostLibs.oneBlock.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class OBBaseEvent extends Event implements Cancellable {

    private boolean cancelled;

    protected OBBaseEvent() {
        super(false);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}