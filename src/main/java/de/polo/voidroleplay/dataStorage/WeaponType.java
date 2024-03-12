package de.polo.voidroleplay.dataStorage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WeaponType {
    NORMAL(true),
    FFA(false),
    GANGWAR(false);


    private boolean needsAmmoToReload;
}
