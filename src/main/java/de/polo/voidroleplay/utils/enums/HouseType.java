package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HouseType {
    BASIC("Standart", 2, false),
    VILLA("Villa", 5, false),
    CARAVAN("Wohnwagen", 2, true);

    private final String name;
    private final int baseSlots;
    private final boolean canCook;
}
