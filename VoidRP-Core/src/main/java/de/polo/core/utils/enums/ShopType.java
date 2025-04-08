package de.polo.core.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ShopType {
    GUN("Waffenladen", 950000),
    VEHICLE("Autohaus", 950000),
    SUPERMARKET("Supermarkt", 950000),
    BLACKMARKET("Schwarzmarkt", 950000),
    GARDENER("Gärtner", 950000),
    FOODSTORE("Lebensmittelgeschäft", 950000),
    BAR("Bar", 950000);

    private final String name;
    private final int price;
}
