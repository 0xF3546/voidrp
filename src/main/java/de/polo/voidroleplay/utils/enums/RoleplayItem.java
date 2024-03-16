package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public enum RoleplayItem {
    JOINT("§2Joint", Material.BAMBOO),
    MARIHUANA("§aMarihuana", Material.KELP),
    BOX_WITH_JOINTS("§7Kiste mit Joints", Material.CHEST),
    COCAINE("§fKokain", Material.SUGAR),
    NOBLE_JOINT("§2Veredelter Joint", Material.BAMBOO),
    WELDING_MACHINE("§cSchweißgerät", Material.BLAZE_ROD),
    CUFF("§6Handschellen", Material.LEAD),
    SWAT_SHIELD("§6Einsatzschild", Material.SHIELD),
    ANTIBIOTIKUM("§cAntibiotikum", Material.RED_DYE),
    DIAMOND("§bDiamant", Material.DIAMOND),
    PEARL("§bPerle", Material.GHAST_TEAR),
    SHELL("§eMuschel", Material.BIRCH_BUTTON),
    EXPLOSION_DEVICE("§cSprengsatz", Material.TNT),
    TAZER("§bTazer", Material.GOLDEN_HOE);

    private final String displayName;
    private final Material material;

    private void Add() {

    }
}
