package de.polo.voidroleplay.utils.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HourTickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private int hour;
    public HourTickEvent(int hour) {
        this.hour = hour;
    }

    public int getHour() {
        return hour;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}