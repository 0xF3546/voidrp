package de.polo.core.oil.entities;

import de.polo.api.company.Company;
import de.polo.api.oil.OilPump;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreOilPump implements OilPump {
    @Getter
    private final int id;

    @Getter
    private final Location location;

    @Getter
    @Setter
    private Company company;

    @Getter
    @Setter
    private int level;

    @Getter
    @Setter
    private int oil;

    public CoreOilPump(int id, Company company, Location location, int level, int oil) {
        this.id = id;
        this.company = company;
        this.location = location;
        this.level = level;
        this.oil = oil;
    }

    @Override
    public int getProductionPerHour() {
        return level * 30;
    }
}
