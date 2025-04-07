package de.polo.voidroleplay.game.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HourTickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final int hour;

    public HourTickEvent(int hour) {
        this.hour = hour;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public int getHour() {
        return hour;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}