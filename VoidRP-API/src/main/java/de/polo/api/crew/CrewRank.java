package de.polo.api.crew;

import de.polo.api.crew.enums.CrewPermission;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CrewRank {
    /**
     * @return the id of the rank
     */
    int getId();

    /**
     * @return the name of the rank
     */
    String getName();

    /**
     * Sets the name of the rank
     */
    void setName(String name);

    /**
     * @return the color of the rank
     */
    TextColor getColor();

    /**
     * @return the crew id of the rank
     */
    int getCrewId();

    /**
     * @return the rank of the rank
     */
    int getRank();

    boolean isDefault();

    boolean isBoss();

    List<CrewPermission> getPermissions();

    void addPermission(CrewPermission permission);

    void removePermission(CrewPermission permission);

    boolean hasPermission(CrewPermission permission);
}
