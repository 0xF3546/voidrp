package de.polo.core.beerpong.services;

import de.polo.api.player.VoidPlayer;
import de.polo.api.zone.Zone;
import de.polo.core.beerpong.handler.BeerPongHandler;

import java.util.List;

public interface BeerPongService {
    List<BeerPongHandler> getGames();
    void addGame(BeerPongHandler game);
    void removeGame(BeerPongHandler game);
    BeerPongHandler getGameByZone(Zone zone);
    BeerPongHandler getHandlerByPlayer(VoidPlayer player);
}
