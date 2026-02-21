package de.polo.api.utils.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central cache manager that holds a {@link GenericCache} for each registered entity type.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * GenericCache<Integer, MyEntity> myCache = Cache.getCache(MyEntity.class);
 * myCache.put(entity.getId(), entity);
 * }</pre>
 *
 * @author VoidRP
 * @version 1.0.0
 * @see GenericCache
 * @see EntityBase
 */
public class Cache {

    private static final Map<Class<?>, GenericCache<?, ?>> caches = new ConcurrentHashMap<>();

    private Cache() {
    }

    /**
     * Returns the {@link GenericCache} for the given entity type, creating it if it does not yet exist.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param type the class of the entity to cache
     * @return the {@link GenericCache} associated with the given type
     * @throws IllegalArgumentException if type is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <K, V> GenericCache<K, V> getCache(Class<V> type) {
        if (type == null) throw new IllegalArgumentException("type cannot be null");
        return (GenericCache<K, V>) caches.computeIfAbsent(type, k -> new GenericCache<>());
    }

    /**
     * Invalidates (clears) the {@link GenericCache} for the given entity type.
     *
     * @param type the class whose cache should be cleared
     * @throws IllegalArgumentException if type is {@code null}
     */
    public static void invalidate(Class<?> type) {
        if (type == null) throw new IllegalArgumentException("type cannot be null");
        GenericCache<?, ?> cache = caches.get(type);
        if (cache != null) {
            cache.invalidate();
        }
    }

    /**
     * Invalidates all caches managed by this class.
     */
    public static void invalidateAll() {
        caches.values().forEach(GenericCache::invalidate);
    }
}
