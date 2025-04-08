package de.polo.core.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@AllArgsConstructor
@Getter
public enum PetStoreItem {
    CHICKEN(Pet.CHICKEN, 30000, PriceType.CASH, Material.CHICKEN_SPAWN_EGG),
    RABBIT(Pet.RABBIT, 90000, PriceType.CASH, Material.RABBIT_SPAWN_EGG),
    BEE(Pet.BEE, 150000, PriceType.CASH, Material.BEE_SPAWN_EGG),
    WOLF(Pet.WOLF, 10, PriceType.VOTES, Material.WOLF_SPAWN_EGG),
    OCELOT(Pet.OCELOT, 30, PriceType.VOTES, Material.OCELOT_SPAWN_EGG),
    BAT(Pet.BAT, 100, PriceType.VOTES, Material.BAT_SPAWN_EGG),
    MUSHROOM_COW(Pet.MUSHROOM_COW, 1, PriceType.RECRUITED, Material.COW_SPAWN_EGG),
    POLAR_BEAR(Pet.POLAR_BEAR, 3, PriceType.RECRUITED, Material.POLAR_BEAR_SPAWN_EGG),
    PARROT(Pet.PARROT, 5, PriceType.RECRUITED, Material.PARROT_SPAWN_EGG);

    private final Pet pet;
    private int price;
    private PriceType priceType;
    private Material material;
}
