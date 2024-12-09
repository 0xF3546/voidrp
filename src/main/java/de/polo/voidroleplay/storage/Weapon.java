package de.polo.voidroleplay.storage;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

public class Weapon {
    private int id;
    private UUID owner;
    @Getter
    @Setter
    private de.polo.voidroleplay.utils.enums.Weapon type;

    @Getter
    @Setter
    private PlayerWeapon playerWeapon;
    private boolean isReloading = false;
    private int ammo;
    private int currentAmmo;
    private WeaponType weaponType;
    private boolean canShoot = true;

    @Getter
    @Setter
    private Instant shootCooldown = Instant.now();

    public boolean isReloading() {
        return isReloading;
    }

    public void setReloading(boolean reloading) {
        isReloading = reloading;
    }

    public int getAmmo() {
        return ammo;
    }

    public void setAmmo(int ammo) {
        this.ammo = ammo;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(WeaponType weaponType) {
        this.weaponType = weaponType;
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }

    public void setCurrentAmmo(int currentAmmo) {
        this.currentAmmo = currentAmmo;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isCanShoot() {
        return canShoot;
    }

    public void setCanShoot(boolean canShoot) {
        this.canShoot = canShoot;
    }
}
