package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@AllArgsConstructor
@Getter
public enum Farmer {

    ARAMID("Aramid", new Location(Bukkit.getWorld("World"), 290, 69, -311), Material.DEAD_BUSH, RoleplayItem.ARAMID, 250);

    private final String name;
    private final Location location;
    private final Material farmingItem;
    private final RoleplayItem outputItem;
    private final int range;
}
