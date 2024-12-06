package de.polo.voidroleplay.game.base.extra;


import de.polo.voidroleplay.utils.enums.IllnessType;
import lombok.Getter;
import lombok.Setter;

public class PlayerIllness {

    @Setter
    @Getter
    private int id;
    @Getter
    private final IllnessType illnessType;

    public PlayerIllness(IllnessType illnessType) {
        this.illnessType = illnessType;
    }
}
