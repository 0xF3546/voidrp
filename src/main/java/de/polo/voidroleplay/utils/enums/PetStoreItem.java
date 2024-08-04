package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.EntityType;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@AllArgsConstructor
@Getter
public enum PetStoreItem {
    CHICKEN(Pet.CHICKEN, 30000, PriceType.CASH),
    RABBIT(Pet.RABBIT, 90000, PriceType.CASH),
    BEE(Pet.BEE, 150000, PriceType.CASH),
    WOLF(Pet.WOLF, 10, PriceType.VOTES),
    OCELOT(Pet.OCELOT, 30, PriceType.VOTES),
    BAT(Pet.BAT, 100, PriceType.VOTES),
    MUSHROOM_COW(Pet.MUSHROOM_COW, 1, PriceType.RECRUITED),
    POLAR_BEAR(Pet.POLAR_BEAR, 3, PriceType.RECRUITED),
    PARROT(Pet.PARROT, 5, PriceType.RECRUITED);

    private final Pet pet;
    private int price;
    private PriceType priceType;
}
