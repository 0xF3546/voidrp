package de.polo.api.zone;

import de.polo.api.player.VoidPlayer;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class ExitZoneEvent extends ZoneEvent {

    public ExitZoneEvent(VoidPlayer player, Zone zone) {
        super(player, zone);
    }
}
