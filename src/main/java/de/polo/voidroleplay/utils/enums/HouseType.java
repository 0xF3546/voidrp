package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HouseType {
    BASIC("Standart", false),
    VILLA("Villa", false),
    CARAVAN("Wohnwagen", true);

    private final String name;
    private final boolean canCook;
}
