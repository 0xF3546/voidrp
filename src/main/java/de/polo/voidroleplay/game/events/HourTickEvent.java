package de.polo.voidroleplay.game.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HourTickEvent extends Event {
    @Getter
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final int hour;
    public HourTickEvent(int hour) {
        this.hour = hour;
    }
}