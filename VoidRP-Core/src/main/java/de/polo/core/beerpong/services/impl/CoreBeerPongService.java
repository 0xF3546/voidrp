package de.polo.core.beerpong.services.impl;

import de.polo.api.player.VoidPlayer;
import de.polo.api.zone.Zone;
import de.polo.core.beerpong.handler.BeerPongHandler;
import de.polo.core.beerpong.entity.BeerPongPlayer;
import de.polo.core.beerpong.services.BeerPongService;
import de.polo.core.utils.Service;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;

import java.util.List;

@Service
public class CoreBeerPongService implements BeerPongService {
    @Getter
    private final List<BeerPongHandler> games = new ObjectArrayList<>();

    @Override
    public void addGame(BeerPongHandler game) {
        BeerPongHandler g = getGameByZone(game.getZone());
        if (g != null) return;
        games.add(game);
    }

    @Override
    public void removeGame(BeerPongHandler game) {
        game.getPlayers().forEach(BeerPongPlayer::unequip);
        games.remove(game);
    }

    @Override
    public BeerPongHandler getGameByZone(Zone zone) {
        return games.stream()
                .filter(game -> game.getZone() == zone)
                .findFirst()
                .orElse(null);
    }

    @Override
    public BeerPongHandler getHandlerByPlayer(VoidPlayer player) {
        return games.stream()
                .filter(handler -> handler.getPlayers().stream()
                        .anyMatch(p -> p.getPlayer().equals(player)))
                .findFirst()
                .orElse(null);
    }

}
