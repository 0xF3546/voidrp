package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public enum RoleplayItem {
    PIPE("§6Pfeife", "Pfeife", Material.BAMBOO, true),
    FACTION_PIPE("§6Pfeife (F)", "Pfeife (F)", Material.BAMBOO, true),
    PIPE_TOBACCO("§2Pfeifentabak", "Pfeifentabak", Material.KELP, true),
    BOX_WITH_JOINTS("§7Kiste mit Pfeifen", "Kiste mit Pfeifen", Material.CHEST, true),
    SNUFF("§7Schnupftabak", "Schnupftabak", Material.SUGAR, true),
    CIGAR("§7§lZigarre", "Zigarre", Material.STICK, true),
    WELDING_MACHINE("§cSchweißgerät", "Schweißgerät", Material.BLAZE_ROD, true),
    CUFF("§6Handschellen", "Handschellen", Material.LEAD, true),
    SWAT_SHIELD("§6Einsatzschild", "Einsatzschild", Material.SHIELD, true),
    ANTIBIOTIKUM("§cAntibiotikum", "Antibiotikum", Material.RED_DYE, false),
    SCHMERZMITTEL("§cSchmerzmittel", "Schmerzmittel", Material.RED_DYE, false),
    CRYSTAL("§bKristall", "Kristall", Material.PRISMARINE_CRYSTALS, true),
    DIAMOND("§bDiamant", "Diamant", Material.DIAMOND, false),
    PEARL("§bPerle", "Perle", Material.GHAST_TEAR, false),
    SHELL("§eMuschel", "Muschel", Material.BIRCH_BUTTON, false),
    EXPLOSION_DEVICE("§cSprengsatz", "Sprengsatz", Material.TNT, true),
    TAZER("§bTazer", "Tazer", Material.GOLDEN_SHOVEL, true),
    CROWBAR("§cBrechstange", "Brechstange", Material.BLAZE_ROD, true),
    ADRENALINE_INJECTION("§fAdrenalin Spritze", "Adrenalin Spritze", Material.END_ROD, true),
    BULLETPROOF("§7Schutzweste", "Schutzweste", Material.LEATHER_CHESTPLATE, false),
    KEVLAR("§7Kevlar", "Kevlar", Material.LEATHER, false),
    ARAMID("§7Aramidfaser", "Aramidfaser", Material.DEAD_BUSH, false),
    MAGAZIN("§7Magazin", "Magazin", Material.CLAY_BALL, true),
    WAFFENTEIL("§8Waffenteil", "Waffenteil", Material.NETHERITE_INGOT, true),
    EISENERZ("§fEisenerz", "Eisenerz", Material.IRON_ORE, false),
    EISEN("§fEisen", "Eisen", Material.IRON_INGOT, false),
    IBOPROFEN("§c§lIboprofen", "Iboprofen", Material.PAPER, false),
    JESUSKREUZ("§6Jesuskreuz", "Jesuskreuz", Material.TOTEM_OF_UNDYING, false),
    MASK("§7Maske", "Maske", Material.WITHER_SKELETON_SKULL, true),
    SMARTPHONE("§eHandy", "Handy", Material.IRON_NUGGET, true),
    HEAVY_BULLETPROOF("§7Schwere Schutzweste", "Schwere Schutzweste", Material.IRON_CHESTPLATE, false),
    MOLOTOV("§7Molotov-Cocktail", "Molotov-Cocktail", Material.FLINT, true),
    FEUERWEHR_AXT("§7Feuerwehraxt", "Feuerwehraxt", Material.IRON_AXE, false),
    SPRUNGTUCH("§7Sprungtuch", "Sprungtuch", Material.STICK, false),
    SPRENGSTOFF("§7Sprengstoff", "Sprengstoff", Material.TNT, true),
    GRANATE("§7Granate", "Granate", Material.FIRE_CHARGE, true),
    SPRENGGUERTEL("§7Sprenggürtel", "Sprenggürtel", Material.LEATHER_CHESTPLATE, true),
    DRAHT("§7Draht", "Draht", Material.PAPER, true),
    WINGSUIT("§7Wingsuit", "Wingsuit", Material.ELYTRA, true),
    ROADBLOCK("§7Roadblock", "Roadblock", Material.STICK, true),
    PFEFFERSPRAY("§7Pfefferspray", "Pfefferspray", Material.LEVER, true),
    MINER_PICKAXE_WOODEN("§7Holz Spitzhacke", "Holz Spitzhacke", Material.WOODEN_PICKAXE, false),
    MINER_PICKAXE_STONE("§7Stein Spitzhacke", "Stein Spitzhacke", Material.STONE_PICKAXE, false),
    MINER_PICKAXE_IRON("§7Eisen Spitzhacke", "Eisen Spitzhacke", Material.IRON_PICKAXE, false),
    MINER_PICKAXE_DIA("§7Diamant Spitzhacke", "Diamant Spitzhacke", Material.DIAMOND_PICKAXE, false),
    FEUERLÖSCHER("§7Feuerlöscher", "Feuerlöscher", Material.LEVER, false),
    COCAINE_SEEDS("§fPulver-Samen", "Pulver-Samen", Material.PUMPKIN_SEEDS, true),
    WEED_SEEDS("§2Gras-Samen", "Gras-Samen", Material.PUMPKIN_SEEDS, true),
    FERTILIZER("§2Dünger", "Dünger", Material.BONE_MEAL, false),
    WATER("§bWasser", "Wasser", Material.WATER_BUCKET, false),
    DRINK_WATER("§bWasser", "Wasser", Material.POTION, false);

    private final String displayName;
    private final String clearName;
    private final Material material;
    private final boolean friskItem;
}
