package de.polo.api.player.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public enum UniversalSkill {
    JOB("Job", Material.WHEAT),
    FINANZEN("Finanzen", Material.GOLD_INGOT),
    WAFFEN("Waffen", Material.DIAMOND_HORSE_ARMOR),;
    private final String name;
    private final Material material;
}
