package de.polo.voidroleplay.game.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SecondTickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private int second;
    public SecondTickEvent(int second) {
        this.second = second;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}