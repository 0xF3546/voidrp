package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@AllArgsConstructor
@Getter
public enum Pet {
    CHICKEN(EntityType.CHICKEN, false),
    RABBIT(EntityType.RABBIT, false),
    BEE(EntityType.BEE, false),
    WOLF(EntityType.WOLF, false),
    OCELOT(EntityType.OCELOT, false),
    BAT(EntityType.BAT, false),
    MUSHROOM_COW(EntityType.MUSHROOM_COW, true),
    POLAR_BEAR(EntityType.POLAR_BEAR, true),
    PARROT(EntityType.PARROT, false);

    private final EntityType animal;
    private boolean isSmall;
}
