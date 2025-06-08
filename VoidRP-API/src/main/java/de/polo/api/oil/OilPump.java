package de.polo.api.oil;

import de.polo.api.company.Company;
import org.bukkit.Location;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface OilPump {
    int getId();
    Company getCompany();
    int getLevel();
    void setLevel(int level);
    int getProductionPerHour();
    int getOil();
    void setOil(int oil);
    Location getLocation();
    default int getMaxOil() {
        return 1000;
    }
    default int getOilPerTick() {
        return 5 * this.getLevel();
    }
}
