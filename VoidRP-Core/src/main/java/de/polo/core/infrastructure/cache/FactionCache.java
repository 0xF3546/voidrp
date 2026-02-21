package de.polo.core.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Caffeine-backed cache for {@link CachedFaction} entries, keyed by faction id ({@code int}).
 *
 * <p>Factions are loaded once at startup and kept indefinitely (no eviction)
 * because there are typically only 5–20 factions and they must always be
 * available without a DB round-trip.
 *
 * @see DirtyCache
 */
public final class FactionCache extends DirtyCache<Integer, CachedFaction> {

    public FactionCache() {
        super(Caffeine.newBuilder().<Integer, CachedFaction>build());
    }
}
