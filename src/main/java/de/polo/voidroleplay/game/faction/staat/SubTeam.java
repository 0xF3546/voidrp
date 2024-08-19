package de.polo.voidroleplay.game.faction.staat;

import lombok.Getter;
import lombok.Setter;

public class SubTeam {
    @Getter
    @Setter
    private int id;

    @Getter
    private final int factionId;

    @Getter
    private final String name;

    public SubTeam(int factionId, String name) {
        this.factionId = factionId;
        this.name = name;
    }
}
