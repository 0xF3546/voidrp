package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MinerBlockType {
    COAL("§8Kohle", Material.COAL_ORE, MinerItem.COAL, 0),
    IRON("§6Eisen", Material.IRON_ORE, MinerItem.IRON, 1),
    GOLD("§6Gold", Material.GOLD_ORE, MinerItem.COAL, 2),
    SMARAGD("§eSmaragd", Material.EMERALD_ORE, MinerItem.COAL, 3),
    DIAMOND("§bDiamant", Material.DIAMOND_ORE, MinerItem.COAL, 4),
    RUBIN("§cRubin", Material.REDSTONE_ORE, MinerItem.COAL, 5),
    WITHER_ORE("§bKristall", Material.NETHER_GOLD_ORE, MinerItem.COAL, 6);

    private final String displayName;
    private final Material block;
    private final MinerItem outputItem;
    private final int order;
}
