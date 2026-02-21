package de.polo.api.utils.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A generic, type-safe in-memory cache backed by a {@link java.util.concurrent.ConcurrentHashMap}.
 *
 * @param <K> the type of the cache key
 * @param <V> the type of the cached value
 * @author VoidRP
 * @version 1.0.0
 * @see Cache
 */
public class GenericCache<K, V> {

    private final Map<K, V> cache = new ConcurrentHashMap<>();

    /**
     * Stores a value in the cache under the given key.
     *
     * @param key   the key, must not be {@code null}
     * @param value the value to cache, must not be {@code null}
     * @throws IllegalArgumentException if key or value is {@code null}
     */
    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("key cannot be null");
        if (value == null) throw new IllegalArgumentException("value cannot be null");
        cache.put(key, value);
    }

    /**
     * Returns the cached value for the given key wrapped in an {@link Optional}.
     *
     * @param key the key to look up
     * @return an {@link Optional} containing the value, or empty if not present
     */
    public Optional<V> get(K key) {
        return Optional.ofNullable(cache.get(key));
    }

    /**
     * Removes the entry with the given key from the cache.
     *
     * @param key the key to remove
     */
    public void remove(K key) {
        cache.remove(key);
    }

    /**
     * Returns {@code true} if the cache contains an entry for the given key.
     *
     * @param key the key to check
     * @return {@code true} if the key is present, {@code false} otherwise
     */
    public boolean contains(K key) {
        return cache.containsKey(key);
    }

    /**
     * Returns an unmodifiable view of all values currently held in this cache.
     *
     * @return unmodifiable collection of cached values
     */
    public Collection<V> values() {
        return Collections.unmodifiableCollection(cache.values());
    }

    /**
     * Removes all entries from this cache.
     */
    public void invalidate() {
        cache.clear();
    }

    /**
     * Returns the number of entries currently held in this cache.
     *
     * @return the cache size
     */
    public int size() {
        return cache.size();
    }
}
