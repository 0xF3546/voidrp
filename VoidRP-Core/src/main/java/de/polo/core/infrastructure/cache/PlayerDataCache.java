package de.polo.core.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Collection;
import java.util.UUID;

/**
 * In-memory player cache backed by <a href="https://github.com/ben-manes/caffeine">Caffeine</a>.
 *
 * <p>Entries are removed automatically 10 minutes after the last access so
 * offline players do not occupy heap indefinitely.  With 400 concurrent
 * players the maximum memory footprint is well within reason.
 *
 * <p>This cache is the <em>single source of truth</em> for all in-progress
 * game state.  Database reads only happen on a cache miss (i.e. when a player
 * first joins and their data has not been pre-loaded yet).
 */
public final class PlayerDataCache {

    /**
     * Maximum number of players kept in memory.
     * 600 gives headroom above the 400-player target.
     */
    private static final int MAX_SIZE = 600;

    private final Cache<UUID, CachedPlayer> cache;

    public PlayerDataCache() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(MAX_SIZE)
                .expireAfterAccess(Duration.ofMinutes(10))
                .build();
    }

    /** Stores or replaces the cached entry for {@code uuid}. */
    public void put(UUID uuid, CachedPlayer player) {
        cache.put(uuid, player);
    }

    /**
     * Returns the cached entry for {@code uuid}, or {@code null} if absent.
     * A {@code null} return means the caller must load the player from the DB.
     */
    public CachedPlayer get(UUID uuid) {
        return cache.getIfPresent(uuid);
    }

    /** Removes the entry for {@code uuid} (called after a final flush on quit). */
    public void invalidate(UUID uuid) {
        cache.invalidate(uuid);
    }

    /** Returns all currently cached players (used by the flush service). */
    public Collection<CachedPlayer> allEntries() {
        return cache.asMap().values();
    }

    /** Returns the number of entries currently in the cache. */
    public long size() {
        return cache.estimatedSize();
    }
}
