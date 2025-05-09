package de.polo.api.laboratory.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LaboratoryType {
    DRUG_LAB("Drogenlabor"),
    WEAPON_LAB("Waffenlabor"),
    MONEY_LAB("Geldwäscherei");

    private final String name;
}
