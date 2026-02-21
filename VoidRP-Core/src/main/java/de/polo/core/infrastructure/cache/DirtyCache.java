package de.polo.core.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.Collection;

/**
 * Generic in-memory cache for {@link DirtyTrackable} entries.
 *
 * <p>Backed by Caffeine, keyed on {@code K}, holding entries of type
 * {@code V}.  Subclasses configure the Caffeine cache in their constructor
 * (size limits, expiry policy, etc.) and pass the built instance to
 * {@link #DirtyCache(Cache)}.
 *
 * <p>This single class replaces the type-specific {@code PlayerDataCache}
 * and {@code FactionCache} wrappers which were identical except for their
 * type parameters.
 *
 * @param <K> the key type (e.g. {@code UUID} for players, {@code Integer} for factions)
 * @param <V> the cached entry type – must implement {@link DirtyTrackable}
 */
public class DirtyCache<K, V extends DirtyTrackable<?>> {

    private final Cache<K, V> cache;

    protected DirtyCache(Cache<K, V> cache) {
        this.cache = cache;
    }

    /** Stores or replaces the cached entry for {@code key}. */
    public void put(K key, V value) {
        cache.put(key, value);
    }

    /**
     * Returns the cached entry for {@code key}, or {@code null} if absent.
     */
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    /** Removes the entry for {@code key}. */
    public void invalidate(K key) {
        cache.invalidate(key);
    }

    /** Returns all currently cached entries (used by the flush service). */
    public Collection<V> allEntries() {
        return cache.asMap().values();
    }

    /** Returns the estimated number of entries currently in the cache. */
    public long size() {
        return cache.estimatedSize();
    }
}
