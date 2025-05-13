package de.polo.core.beerpong.listeners;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.api.zone.EnterZoneEvent;
import de.polo.core.beerpong.services.BeerPongService;
import de.polo.core.beerpong.handler.BeerPongHandler;
import de.polo.core.beerpong.entity.BeerPongPlayer;
import de.polo.core.beerpong.entity.BeerPongTeam;
import de.polo.core.utils.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Event
public class EnterBeerPongZoneListener implements Listener {
    @EventHandler
    public void onBeerPongZoneEnter(EnterZoneEvent event) {
        if (!event.getZone().getName().contains("beerpong")) return;
        VoidPlayer player = event.getPlayer();
        BeerPongService beerPongService = VoidAPI.getService(BeerPongService.class);
        BeerPongHandler beerPongHandler;
        if (beerPongService.getGameByZone(event.getZone()) == null) {
            beerPongHandler = new BeerPongHandler(event.getZone());
        } else {
            beerPongHandler = beerPongService.getGameByZone(event.getZone());
        }
        boolean isRedTeamAvailable =  beerPongHandler.getPlayers().stream().anyMatch(x -> x.getTeam().isRed());
        BeerPongPlayer beerPongPlayer = new BeerPongPlayer(player);
        BeerPongTeam team = new BeerPongTeam(beerPongPlayer, isRedTeamAvailable);
        beerPongPlayer.setTeam(team);
        if (!beerPongHandler.addPlayer(beerPongPlayer)) return;
        if (beerPongService.getGameByZone(event.getZone()) == null) {
            beerPongService.addGame(beerPongHandler);
        }
        player.sendMessage("Du bist dem BeerPong Spiel beigetreten!", Prefix.BEERPONG);
    }
}
