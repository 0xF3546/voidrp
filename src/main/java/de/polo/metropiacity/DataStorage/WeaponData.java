package de.polo.metropiacity.DataStorage;

import org.bukkit.Material;
import org.bukkit.Sound;

public class WeaponData {
    private int id;
    private Material material;
    private String name;
    private int maxAmmo;
    private float reloadDuration;
    private float damage;
    private Sound weaponSound;
    private float arrowVelocity;
    private float shootDuration;
    private String type;
    private Material ammoItem;

    public float getReloadDuration() {
        return reloadDuration;
    }

    public void setReloadDuration(float reloadDuration) {
        this.reloadDuration = reloadDuration;
    }

    public int getMaxAmmo() {
        return maxAmmo;
    }

    public void setMaxAmmo(int maxAmmo) {
        this.maxAmmo = maxAmmo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public Sound getWeaponSound() {
        return weaponSound;
    }

    public void setWeaponSound(Sound weaponSound) {
        this.weaponSound = weaponSound;
    }

    public float getArrowVelocity() {
        return arrowVelocity;
    }

    public void setArrowVelocity(float arrowVelocity) {
        this.arrowVelocity = arrowVelocity;
    }

    public float getShootDuration() {
        return shootDuration;
    }

    public void setShootDuration(float shootDuration) {
        this.shootDuration = shootDuration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Material getAmmoItem() {
        return ammoItem;
    }

    public void setAmmoItem(Material ammoItem) {
        this.ammoItem = ammoItem;
    }
}
