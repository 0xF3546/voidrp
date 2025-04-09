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
abstract class EnterZoneEvent extends ZoneEvent {

    protected EnterZoneEvent(VoidPlayer player, Zone zone) {
        super(player, zone);
    }
}
