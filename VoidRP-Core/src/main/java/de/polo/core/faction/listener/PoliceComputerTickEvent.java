package de.polo.core.faction.listener;

import de.polo.core.faction.entity.PoliceComputerHack;
import de.polo.core.game.events.SecondTickEvent;
import de.polo.core.utils.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Event
public class PoliceComputerTickEvent implements Listener {
    @EventHandler
    public void onPoliceComputerTickSecond(SecondTickEvent event) {
        if (!PoliceComputerHack.isActive) return;
        PoliceComputerHack.doTick();
    }
}
