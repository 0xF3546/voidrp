package de.polo.core.beerpong.listeners;

import de.polo.api.utils.enums.Prefix;
import de.polo.api.player.VoidPlayer;
import de.polo.api.zone.ExitZoneEvent;
import de.polo.api.zone.enums.ZoneType;
import de.polo.core.beerpong.utils.BeerPongUtils;
import de.polo.core.utils.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Event
public class ExitBeerPongZoneListener implements Listener {

    @EventHandler
    public void onExitBeerPongZone(ExitZoneEvent event) {
        if (!event.getZone().getType().equals(ZoneType.BEERPONG)) return;
        VoidPlayer player = event.getPlayer();
        BeerPongUtils.handlePlayerLeave(player);
        player.sendMessage("Du hast das BeerPong Spiel verlassen!", Prefix.BEERPONG);
    }
}
