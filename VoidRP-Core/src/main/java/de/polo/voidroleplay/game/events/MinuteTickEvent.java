package de.polo.voidroleplay.game.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MinuteTickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final int minute;

    public MinuteTickEvent(int minute) {
        this.minute = minute;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public int getMinute() {
        return minute;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}