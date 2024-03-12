package de.polo.voidroleplay.utils.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MinuteTickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private int minute;
    public MinuteTickEvent(int minute) {
        this.minute = minute;
    }

    public int getMinute() {
        return minute;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}