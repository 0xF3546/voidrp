package de.polo.core.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@Getter
@AllArgsConstructor
public enum PickaxeType {
    WOOD("§fHolzspitzhacke", Material.WOODEN_PICKAXE, 0, 0),
    STONE("§7Steinspitzhacke", Material.STONE_PICKAXE, 1, 1),
    IRON("§6Eisenspitzhacke", Material.IRON_PICKAXE, 2, 2),
    DIAMOND("§bDiamantspitzhacke", Material.DIAMOND_PICKAXE, 3, 3),
    SMARAGD("§aSmaragtspitzhacke", Material.DIAMOND_PICKAXE, 4, 4),
    RUBIN("§cRubinspitzhacke", Material.DIAMOND_PICKAXE, 5, 5),
    CRYSTAL("§bKristallspitzhacke", Material.DIAMOND_PICKAXE, 6, 6);

    private String displayName;
    private Material material;
    private int minLevel;
    private int order;
}
