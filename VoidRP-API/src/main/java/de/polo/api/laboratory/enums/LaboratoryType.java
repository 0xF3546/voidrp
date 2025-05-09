package de.polo.api.laboratory.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LaboratoryType {
    DRUG_LAB("Drug Lab"),
    WEAPON_LAB("Weapon Lab"),
    MONEY_LAB("Money Lab");

    private final String name;
}
