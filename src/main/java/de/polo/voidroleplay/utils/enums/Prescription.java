package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Prescription {
    SCHMERZMITTEL("Schmerzmittel", Drug.SCHMERZMITTEL, 8, 12);
    private final String name;
    private final Drug drug;
    private final int minAmount;
    private final int maxAmount;
}
