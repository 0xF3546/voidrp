package de.polo.core.storage;

import de.polo.core.game.faction.apotheke.Apotheke;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * Manages the takeout records for Apotheke entities.
 *
 * @author Mayson1337
 * @version 1.1.0
 * @since 1.0.0
 */
public class ApothekeTakeOut {

    // Static list to store takeout records
    private static final List<ApothekeTakeOut> takeouts = new ObjectArrayList<>();
    @Getter
    private final Apotheke apotheke;
    @Getter
    private final UUID uuid;

    /**
     * Constructor for creating a takeout record.
     *
     * @param apotheke The associated Apotheke instance.
     * @param uuid     The UUID of the user.
     */
    public ApothekeTakeOut(Apotheke apotheke, UUID uuid) {
        this.apotheke = apotheke;
        this.uuid = uuid;
    }

    /**
     * Adds a new takeout record.
     *
     * @param apotheke The associated Apotheke instance.
     * @param uuid     The UUID of the user.
     */
    public static void add(Apotheke apotheke, UUID uuid) {
        takeouts.add(new ApothekeTakeOut(apotheke, uuid));
    }

    /**
     * Checks if a specific user has already taken out something from the given Apotheke.
     *
     * @param apotheke The Apotheke instance.
     * @param uuid     The UUID of the user.
     * @return True if the user has taken out something, false otherwise.
     */
    public static boolean tookOut(Apotheke apotheke, UUID uuid) {
        return takeouts.stream().anyMatch(record ->
                record.apotheke.equals(apotheke) && record.uuid.equals(uuid));
    }

    /**
     * Clears all takeout records related to a specific Apotheke.
     *
     * @param apotheke The Apotheke instance.
     */
    public static void clear(Apotheke apotheke) {
        takeouts.removeIf(record -> record.apotheke.equals(apotheke));
    }
}
