package de.polo.core.oil.entities;

import de.polo.api.company.Company;
import de.polo.api.oil.OilPump;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreOilPump implements OilPump {
    @Getter
    private final int id;

    @Getter
    @Setter
    private Company company;

    @Getter
    @Setter
    private int level;

    @Getter
    @Setter
    private int productionPerHour;

    @Getter
    @Setter
    private int oil;

    public CoreOilPump(int id, Company company, int level, int productionPerHour, int oil) {
        this.id = id;
        this.company = company;
        this.level = level;
        this.productionPerHour = productionPerHour;
        this.oil = oil;
    }
}
