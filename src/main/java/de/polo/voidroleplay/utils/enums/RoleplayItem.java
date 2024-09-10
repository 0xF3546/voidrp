package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public enum RoleplayItem {
    PIPE("§6Pfeife", Material.BAMBOO, true),
    FACTION_PIPE("§6Pfeife (F)", Material.BAMBOO, true),
    PIPE_TOBACCO("§2Pfeifentabak", Material.KELP, true),
    BOX_WITH_JOINTS("§7Kiste mit Pfeifen", Material.CHEST, true),
    SNUFF("§7Schnupftabak", Material.SUGAR, true),
    CIGAR("§7§lZigarre", Material.STICK, true),
    WELDING_MACHINE("§cSchweißgerät", Material.BLAZE_ROD, true),
    CUFF("§6Handschellen", Material.LEAD, true),
    SWAT_SHIELD("§6Einsatzschild", Material.SHIELD, true),
    ANTIBIOTIKUM("§cAntibiotikum", Material.RED_DYE, false),
    SCHMERZMITTEL("§cSchmerzmittel", Material.RED_DYE, false),
    CRYSTAL("§bKristall", Material.PRISMARINE_CRYSTALS, true),
    DIAMOND("§bDiamant", Material.DIAMOND, false),
    PEARL("§bPerle", Material.GHAST_TEAR, false),
    SHELL("§eMuschel", Material.BIRCH_BUTTON, false),
    EXPLOSION_DEVICE("§cSprengsatz", Material.TNT, true),
    TAZER("§bTazer", Material.GOLDEN_HOE, true),
    CROWBAR("§cBrechstange", Material.BLAZE_ROD, true),
    ADRENALINE_INJECTION("§fAdrenalin Spritze", Material.END_ROD, true),
    BULLETPROOF("§7Schutzweste", Material.LEATHER_CHESTPLATE, false),
    KEVLAR("§7Kevlar", Material.LEATHER, false),
    ARAMID("§7Aramidfaser", Material.DEAD_BUSH, false),
    MAGAZIN("§7Magazin", Material.CLAY_BALL, true),
    WAFFENTEIL("§8Waffenteil", Material.NETHERITE_INGOT, true),
    EISENERZ("§fEisenerz", Material.IRON_ORE, false),
    EISEN("§fEisen", Material.IRON_INGOT, false),
    IBOPROFEN("§c§lIboprofen", Material.PAPER, false),
    JESUSKREUZ("§6Jesuskreuz", Material.TOTEM_OF_UNDYING, false),
    MASK("§7Maske", Material.WITHER_SKELETON_SKULL, true),
    SMARTPHONE("§eHandy", Material.IRON_NUGGET, true),
    HEAVY_BULLETPROOF("§7Schwere Schutzweste", Material.IRON_CHESTPLATE, false),
    MOLOTOV("§7Molotov-Cocktail", Material.FLINT, true),
    FEUERWEHR_AXT("§7Feuerwehraxt", Material.IRON_AXE, false),
    SPRUNGTUCH("§7Sprungtuch", Material.STICK, false),
    SPRENGSTOFF("§7Sprengstoff", Material.TNT, true),
    GRANATE("§7Granate", Material.FIRE_CHARGE, true),
    SPRENGGUERTEL("§7Sprenggürtel", Material.LEATHER_CHESTPLATE, true),
    DRAHT("§7Draht", Material.PAPER, true),
    WINGSUIT("§7Wingsuit", Material.ELYTRA, true),
    ROADBLOCK("§7Roadblock", Material.STICK, true),
    PFEFFERSPRAY("§7Pfefferspray", Material.LEVER, true),
    MINER_PICKAXE_WOODEN("§7Holz Spitzhacke", Material.WOODEN_PICKAXE, false),
    MINER_PICKAXE_STONE("§7Stein Spitzhacke", Material.STONE_PICKAXE, false),
    MINER_PICKAXE_IRON("§7Eisen Spitzhacke", Material.IRON_PICKAXE, false),
    MINER_PICKAXE_DIA("§7Diamant Spitzhacke", Material.DIAMOND_PICKAXE, false),
    FEUERLÖSCHER("§7Feuerlöscher", Material.LEVER, false);

    private final String displayName;
    private final Material material;
    private boolean friskItem;

    private void Add() {

    }
}
