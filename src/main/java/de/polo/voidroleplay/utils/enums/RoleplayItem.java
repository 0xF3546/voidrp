package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public enum RoleplayItem {
    JOINT("§2Joint", Material.BAMBOO, true),
    MARIHUANA("§aMarihuana", Material.KELP, true),
    BOX_WITH_JOINTS("§7Kiste mit Joints", Material.CHEST, true),
    COCAINE("§fKokain", Material.SUGAR, true),
    NOBLE_JOINT("§2Veredelter Joint", Material.BAMBOO, true),
    WELDING_MACHINE("§cSchweißgerät", Material.BLAZE_ROD, true),
    CUFF("§6Handschellen", Material.LEAD, false),
    SWAT_SHIELD("§6Einsatzschild", Material.SHIELD, false),
    ANTIBIOTIKUM("§cAntibiotikum", Material.RED_DYE, false),
    DIAMOND("§bDiamant", Material.DIAMOND, false),
    PEARL("§bPerle", Material.GHAST_TEAR, false),
    SHELL("§eMuschel", Material.BIRCH_BUTTON, false),
    EXPLOSION_DEVICE("§cSprengsatz", Material.TNT, true),
    TAZER("§bTazer", Material.GOLDEN_HOE, false);

    private final String displayName;
    private final Material material;
    private boolean friskItem;

    private void Add() {

    }
}
