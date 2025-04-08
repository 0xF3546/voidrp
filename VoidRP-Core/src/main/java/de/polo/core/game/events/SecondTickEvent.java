package de.polo.core.game.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SecondTickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final int second;

    public SecondTickEvent(int second) {
        this.second = second;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}