package de.polo.api.faction.gangwar;

import java.sql.Timestamp;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IGangzone {
    String getName();

    void setName(String zone);

    String getOwner();

    void setOwner(String faction);

    int getId();

    void setId(int id);

    Timestamp getLastAttack();

    void setLastAttack(Timestamp attack);
}
