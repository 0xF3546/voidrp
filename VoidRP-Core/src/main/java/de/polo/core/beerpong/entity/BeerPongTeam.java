package de.polo.core.beerpong.entity;

import lombok.Getter;
import lombok.Setter;

public class BeerPongTeam {
    @Getter
    private final boolean isRed;

    @Getter
    private final BeerPongPlayer player;

    @Getter
    @Setter
    private int score = 0;

    public BeerPongTeam(BeerPongPlayer player, boolean isRed) {
        this.player = player;
        this.isRed = isRed;
    }
}
