package de.polo.voidroleplay.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WeaponType {
    NORMAL(true),
    FFA(false),
    GANGWAR(false),
    MILITARY(false);


    private boolean needsAmmoToReload;
}
