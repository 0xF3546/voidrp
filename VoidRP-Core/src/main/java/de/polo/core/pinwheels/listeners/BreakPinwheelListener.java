package de.polo.core.pinwheels.listeners;

import de.polo.api.VoidAPI;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.pinwheels.services.PinwheelService;
import de.polo.core.utils.Event;
import de.polo.core.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Event
public class BreakPinwheelListener implements Listener {
    @EventHandler
    public void BreakPinwheel(MinuteTickEvent event) {
        if (event.getMinute() % 5 != 0) return;
        PinwheelService pinwheelService = VoidAPI.getService(PinwheelService.class);
        pinwheelService.getPinwheels().forEach(x -> {
            if (Utils.random(1, 3) != 2) return;
            x.setBroken(true);
        });
    }
}
