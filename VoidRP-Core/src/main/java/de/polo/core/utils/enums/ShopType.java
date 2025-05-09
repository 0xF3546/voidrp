package de.polo.core.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ShopType {
    GUN("Waffenladen", 950000, true, true, true),
    VEHICLE("Autohaus", 950000, true, false, false),
    SUPERMARKET("Supermarkt", 950000, true, true, true),
    BLACKMARKET("Schwarzmarkt", 950000, false, false, false),
    GARDENER("Gärtner", 950000, true, true, true),
    FOODSTORE("Lebensmittelgeschäft", 950000, true, true, true),
    BAR("Bar", 950000, false, true, true);

    private final String name;
    private final int price;
    private final boolean isRobable;
    private final boolean isTakeable;
    private boolean isBuyable;
}
