package de.polo.voidroleplay.game.base.extra;


import de.polo.voidroleplay.utils.enums.IllnessType;
import lombok.Getter;
import lombok.Setter;

public class PlayerIllness {

    @Getter
    private final IllnessType illnessType;
    @Setter
    @Getter
    private int id;

    public PlayerIllness(IllnessType illnessType) {
        this.illnessType = illnessType;
    }
}
