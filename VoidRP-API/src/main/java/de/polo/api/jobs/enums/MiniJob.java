package de.polo.api.jobs.enums;

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
public enum MiniJob {
    WASTE_COLLECTOR("Müllsammler", Material.CAULDRON),
    BOTTLE_TRANSPORT("Flaschenfahrer", Material.GLASS_BOTTLE),
    SEWER_CLEANER("Kanalreiniger", Material.BRUSH),
    FARMER("Farmer", Material.WHEAT),
    WHEAT_TRANSPORT("Weizenfahrer", Material.WHEAT),
    DEEP_SEA_FISHERMAN("Hochseefischer", Material.FISHING_ROD),
    FOOD_SUPPLIER("Lebensmittellieferant", Material.COOKED_BEEF),
    LUMBERJACK("Holzfäller", Material.OAK_LOG),
    MINER("Bergarbeiter", Material.STONE),
    POSTMAN("Postbote", Material.BOOK),
    UNDERTAKER("Bestatter", Material.SKELETON_SKULL),
    URANIUM_MINER("Uranbergbauer", Material.EMERALD),
    WINZER("Winzer", Material.MAGENTA_DYE),
    EQUIP_TRANSPORT("Ausrüstungsfahrer", Material.IRON_SWORD),
    ELECTRITION("Elektriker", Material.REDSTONE),;
    private final String name;
    private final Material icon;
}
