package de.polo.voidroleplay.game.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MinuteTickEvent extends Event {
    @Getter
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final int minute;
    public MinuteTickEvent(int minute) {
        this.minute = minute;
    }

}