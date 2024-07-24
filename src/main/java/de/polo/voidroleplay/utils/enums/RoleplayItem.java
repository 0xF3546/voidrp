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
    SCHMERZMITTEL("§cSchmerzmittel", Material.RED_DYE, false),
    DIAMOND("§bDiamant", Material.DIAMOND, false),
    PEARL("§bPerle", Material.GHAST_TEAR, false),
    SHELL("§eMuschel", Material.BIRCH_BUTTON, false),
    EXPLOSION_DEVICE("§cSprengsatz", Material.TNT, true),
    TAZER("§bTazer", Material.GOLDEN_HOE, false),
    CROWBAR("§cBrechstange", Material.BLAZE_ROD, true),
    ADRENALINE_INJECTION("§fAdrenalin Spritze", Material.END_ROD, true),
    BULLETPROOF("§7Schutzweste", Material.LEATHER_CHESTPLATE, false),
    KEVLAR("§7Kevlar", Material.LEATHER, false),
    ARAMID("§7Aramidfaser", Material.DEAD_BUSH, false),
    MAGAZIN("§7Magazin", Material.CLAY_BALL, true),
    WAFFENTEIL("§8Waffenteil", Material.NETHERITE_INGOT, true),
    EISENERZ("§fEisenerz", Material.IRON_ORE, false),
    EISEN("§fEisen", Material.IRON_INGOT, false),
    IBOPROFEN("§c§lIboprofen", Material.PAPER, false);

    private final String displayName;
    private final Material material;
    private boolean friskItem;

    private void Add() {

    }
}
