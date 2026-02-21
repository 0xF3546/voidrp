package de.polo.core.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Collection;

/**
 * In-memory faction cache backed by Caffeine.
 *
 * <p>Factions are loaded once at startup and kept indefinitely (no eviction)
 * because faction data must always be available instantly and there are
 * typically only 5–20 factions on an RP server.  The low entry count means
 * the full dataset fits comfortably in heap without size/time limits.
 */
public final class FactionCache {

    private final Cache<Integer, CachedFaction> cache;

    public FactionCache() {
        this.cache = Caffeine.newBuilder()
                .build();
    }

    /** Stores or replaces the cached entry for the given faction id. */
    public void put(int factionId, CachedFaction faction) {
        cache.put(factionId, faction);
    }

    /**
     * Returns the cached {@link CachedFaction} for {@code factionId},
     * or {@code null} if absent.
     */
    public CachedFaction get(int factionId) {
        return cache.getIfPresent(factionId);
    }

    /** Returns all currently cached factions (used by the flush service). */
    public Collection<CachedFaction> allEntries() {
        return cache.asMap().values();
    }

    /** Returns the number of factions currently held in the cache. */
    public long size() {
        return cache.estimatedSize();
    }

    /** Removes the entry for {@code factionId}. */
    public void invalidate(int factionId) {
        cache.invalidate(factionId);
    }
}
