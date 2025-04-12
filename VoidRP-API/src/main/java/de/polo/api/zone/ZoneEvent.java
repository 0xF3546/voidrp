package de.polo.api.zone;

import de.polo.api.player.VoidPlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
abstract class ZoneEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final VoidPlayer player;
    @Getter
    private final Zone zone;
    protected ZoneEvent(VoidPlayer player, Zone zone) {
        this.player = player;
        this.zone = zone;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
