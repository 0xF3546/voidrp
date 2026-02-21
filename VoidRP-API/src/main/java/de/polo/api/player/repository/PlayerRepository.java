package de.polo.api.player.repository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Async repository interface for player persistence.
 *
 * <p>Reads are cache-first; writes only mark the player dirty.
 * The FlushService persists dirty entries in batch every ~10 seconds,
 * on player quit, and on server stop – never per-call.
 */
public interface PlayerRepository {

    /**
     * Looks up the raw money balance from the underlying store.
     * For in-memory operations, prefer the cache directly.
     *
     * @param uuid the player's unique identifier
     * @return future with the player's cash balance, or empty if not found
     */
    CompletableFuture<Integer> findBargeld(UUID uuid);

    /**
     * Looks up the raw bank balance from the underlying store.
     *
     * @param uuid the player's unique identifier
     * @return future with the player's bank balance, or empty if not found
     */
    CompletableFuture<Integer> findBank(UUID uuid);

    /**
     * Marks the given player dirty so the FlushService will persist it.
     *
     * @param uuid the player's unique identifier
     */
    void markDirty(UUID uuid);

    /**
     * Immediately persists all currently dirty players in a single transaction.
     * Called by the FlushService and on server stop.
     *
     * @return future that completes when the flush is done
     */
    CompletableFuture<Void> flushDirty();
}
