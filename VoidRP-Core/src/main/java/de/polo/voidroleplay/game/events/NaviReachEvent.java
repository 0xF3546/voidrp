package de.polo.voidroleplay.game.events;

import de.polo.voidroleplay.player.entities.VoidPlayer;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NaviReachEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final VoidPlayer player;

    @Getter
    private final String navi;

    @Getter
    private final Location location;

    public NaviReachEvent(VoidPlayer player, String navi, Location location) {
        this.player = player;
        this.navi = navi;
        this.location = location;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
