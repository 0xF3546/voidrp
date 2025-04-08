package de.polo.core.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@AllArgsConstructor
@Getter
public enum PlantType {
    COCAINE("Pulver", Drug.COCAINE, 180, RoleplayItem.COCAINE_SEEDS),
    WEED("Gras", Drug.JOINT, 120, RoleplayItem.WEED_SEEDS);

    private final String name;
    private final Drug drug;
    private final int time;
    private final RoleplayItem plantItem;

}
