package de.polo.voidroleplay.jobs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum MiniJob {
    WASTE_COLLECTOR("Müllsammler"),
    BOTTLE_TRANSPORT("Flaschenfahrer"),
    SEWER_CLEANER("Kanalreiniger"),
    FARMER("Farmer"),
    DEEP_SEA_FISHERMAN("Hochseefischer"),
    FOOD_SUPPLIER("Lebensmittellieferant"),
    LUMBERJACK("Holzfäller"),
    MINER("Bergarbeiter"),
    POSTMAN("Postbote"),
    UNDERTAKER("Bestatter"),
    URANIUM_MINER("Uranbergbauer"),
    WINZER("Winzer"),;
    private final String name;
}
