package de.polo.core.storage;

import de.polo.core.Main;
import de.polo.core.utils.enums.Weapon;
import lombok.Getter;
import lombok.Setter;

public class PlayerWeapon {
    @Getter
    private final Weapon weapon;
    @Getter
    private final WeaponType weaponType;
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private int wear;
    @Getter
    @Setter
    private int ammo;

    public PlayerWeapon(int id, Weapon weapon, int wear, int ammo, WeaponType weaponType) {
        this.id = id;
        this.weapon = weapon;
        this.wear = wear;
        this.ammo = ammo;
        this.weaponType = weaponType;
    }

    public PlayerWeapon(Weapon weapon, int wear, int ammo, WeaponType weaponType) {
        this.weapon = weapon;
        this.wear = wear;
        this.ammo = ammo;
        this.weaponType = weaponType;
    }

    public void save() {
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE player_gun_cabinet SET wear = ?, ammo = ? WHERE id = ?", wear, ammo, id);
    }
}
