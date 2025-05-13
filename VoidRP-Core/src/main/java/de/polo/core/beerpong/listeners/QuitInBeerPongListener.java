package de.polo.core.beerpong.listeners;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.beerpong.utils.BeerPongUtils;
import de.polo.core.utils.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@Event
public class QuitInBeerPongListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        VoidPlayer player = VoidAPI.getPlayer(event.getPlayer());

        BeerPongUtils.handlePlayerLeave(player);
    }
}
