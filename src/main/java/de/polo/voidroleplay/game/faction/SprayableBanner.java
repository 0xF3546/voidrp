package de.polo.voidroleplay.game.faction;

import de.polo.voidroleplay.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class SprayableBanner {

    @Getter
    @Setter
    private int id;
    @Getter
    private final int registeredBlock;

    @Getter
    @Setter
    private int faction;

    @Getter
    @Setter
    private LocalDateTime lastSpray = Utils.getTime().minusMinutes(10);

    public SprayableBanner(int registeredBlock, int faction) {
        this.registeredBlock = registeredBlock;
        this.faction = faction;
    }

    public void update() {

    }
}
