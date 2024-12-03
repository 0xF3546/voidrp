package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;

@AllArgsConstructor
@Getter
public enum Weapon {
    ASSAULT_RIFLE("§cSturmgewehr", Material.DIAMOND_HORSE_ARMOR, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.7f, 25, 12, 1.25f, 4, 4, 0, false),
    HUNTING_RIFLE("§cJagdgewehr", Material.GOLDEN_HORSE_ARMOR, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0f, 6, 24, 2.5f, 5, 30, 0, false),
    PISTOL("§aPistole", Material.GOLDEN_SHOVEL, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 12, 18, 2, 3, 7, 0, false),
    MARKSMAN("§cMarksman", Material.LEATHER_HORSE_ARMOR, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.9f, 4, 24, 3, 5, 8, 0, false),
    SNIPER("§7Scharfschützengewehr", Material.STONE_HOE, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.2f, 4, 60, 4, 4, 50, 0, false);


    private final String name;
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
}
