package de.polo.voidroleplay.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@AllArgsConstructor
@Getter
public enum Pet {
    CHICKEN(EntityType.CHICKEN, false, "§eHuhn"),
    RABBIT(EntityType.RABBIT, false, "§eHase"),
    BEE(EntityType.BEE, false, "§eBiene"),
    WOLF(EntityType.WOLF, false, "§eHund"),
    OCELOT(EntityType.OCELOT, false, "§eKatze"),
    BAT(EntityType.BAT, false, "§eFledermaus"),
    MUSHROOM_COW(EntityType.MUSHROOM_COW, true, "§ePilz Kuh"),
    POLAR_BEAR(EntityType.POLAR_BEAR, true, "§eEisbär"),
    PARROT(EntityType.PARROT, false, "§ePapagei");

    private final EntityType animal;
    private boolean isSmall;
    private String displayname;
}
