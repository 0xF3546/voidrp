package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Getter
public enum Drug {
    COCAINE(RoleplayItem.COCAINE, Arrays.asList(
            new PotionEffect(PotionEffectType.ABSORPTION, 3 * 60 * 20, 4),
            new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1),
            new PotionEffect(PotionEffectType.REGENERATION, 15 * 12, 0)

    )),
    JOINT(RoleplayItem.NOBLE_JOINT, Arrays.asList(
            new PotionEffect(PotionEffectType.ABSORPTION, 2 * 60 * 20, 1),
            new PotionEffect(PotionEffectType.REGENERATION, 15 * 20, 1)
    )),
    ANTIBIOTIKUM(RoleplayItem.ANTIBIOTIKUM, Arrays.asList(
            new PotionEffect(PotionEffectType.ABSORPTION, 3 * 60 * 20, 3),
            new PotionEffect(PotionEffectType.REGENERATION, 15 * 20, 1)
    )),
    SCHMERZMITTEL(RoleplayItem.SCHMERZMITTEL, Arrays.asList(
            new PotionEffect(PotionEffectType.ABSORPTION, 3 * 60 * 20, 3),
            new PotionEffect(PotionEffectType.REGENERATION, 15 * 20, 1)
    )),
    ADRENALINE_INJECTION(RoleplayItem.ADRENALINE_INJECTION, Arrays.asList(
            new PotionEffect(PotionEffectType.ABSORPTION, 3 * 60 * 20, 4),
            new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1),
            new PotionEffect(PotionEffectType.REGENERATION, 15 * 12, 0)
    ));

    private final RoleplayItem item;
    private final List<PotionEffect> effects;
}
