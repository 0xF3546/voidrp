package de.polo.api.faction.repository;

import de.polo.api.faction.Faction;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Async repository contract for {@link Faction} persistence.
 *
 * <p>All reads are cache-first.  Write operations ({@link #save}) only mark the
 * faction dirty; the FlushService persists the batch periodically so that
 * frequent bank updates (jobs, payday, transfers) do not generate one
 * {@code UPDATE} per call.
 *
 * <p>Implementations live in {@code VoidRP-Core} and must not be referenced
 * from this API module.
 */
public interface FactionRepository {

    /**
     * Returns all factions currently held in the cache.
     * The initial load happens at startup; this method is always synchronous.
     *
     * @return snapshot of every cached faction
     */
    Collection<Faction> findAll();

    /**
     * Looks up a faction by its numeric primary key.
     *
     * @param id the faction's database id
     * @return future containing the faction, or empty if not found
     */
    CompletableFuture<Optional<Faction>> findById(int id);

    /**
     * Looks up a faction by its short name (case-insensitive).
     *
     * @param name the faction's short name (e.g. {@code "Polizei"})
     * @return future containing the faction, or empty if not found
     */
    CompletableFuture<Optional<Faction>> findByName(String name);

    /**
     * Marks the given faction dirty so the FlushService will persist it on
     * the next scheduled flush cycle.
     *
     * <p>This is the preferred write path for frequent bank mutations.
     * No SQL is executed during this call.
     *
     * @param factionId the faction's database id
     */
    void markDirty(int factionId);

    /**
     * Immediately persists all currently dirty factions in a single
     * Hibernate transaction.  Called by the FlushService on schedule, on
     * server stop, and can be called explicitly for important events.
     *
     * @return future that completes when the flush is done
     */
    CompletableFuture<Void> flushDirty();
}
