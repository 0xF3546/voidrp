package de.polo.core.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@AllArgsConstructor
@Getter
public enum MinerItem {
    COAL("§8Kohle", Material.COAL, 3),
    IRON("§7Eisen", Material.IRON_INGOT, 5);

    private final String displayName;
    private final Material material;
    private final int price;
}
