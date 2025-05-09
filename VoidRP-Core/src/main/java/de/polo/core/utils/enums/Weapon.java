package de.polo.core.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;

@AllArgsConstructor
@Getter
public enum Weapon {
    ASSAULT_RIFLE("§cPhantom-4", "Phantom-4", Material.DIAMOND_HORSE_ARMOR, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.7f, 25, 12, 1.25f, 4, 4, 0, false, 250),
    HUNTING_RIFLE("§cDoombringer", "Doombringer", Material.GOLDEN_HOE, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0f, 6, 24, 2.5f, 5, 30, 0, false, 75),
    PISTOL("§aShorty", "Shorty", Material.IRON_HORSE_ARMOR, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 12, 18, 2, 3, 7, 0, false, 300),
    MARKSMAN("§cVortex-10", "Vortex-10", Material.LEATHER_HORSE_ARMOR, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.9f, 4, 24, 3, 5, 8, 0, false, 5),
    SNIPER("§7Scharfschützengewehr", "Scharfschützengewehr", Material.STONE_HOE, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.2f, 4, 60, 4, 4, 50, 0, false, 5),
    SHOTGUN("§eXF119", "XF119", Material.IRON_HOE, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.2f, 4, 60, 4, 4, 50, 0, false, 25);


    private final String name;
    private final String clearName;
    private final Material material;
    private final Sound sound;
    private final float soundPitch;
    private final int maxAmmo;
    private final float reloadDuration;
    private final float damage;
    private final float velocity;
    private final int shootDuration;
    private final float knockback;
    private final boolean meele;
    private final int baseWear;
}
