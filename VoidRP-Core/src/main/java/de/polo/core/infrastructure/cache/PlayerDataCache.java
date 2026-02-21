package de.polo.core.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.UUID;

/**
 * Caffeine-backed cache for {@link CachedPlayer} entries, keyed by {@link UUID}.
 *
 * <p>Entries expire 10 minutes after last access so offline players do not
 * occupy heap indefinitely.  600-entry cap gives headroom above the
 * 400-player target.
 *
 * @see DirtyCache
 */
public final class PlayerDataCache extends DirtyCache<UUID, CachedPlayer> {

    private static final int MAX_SIZE = 600;

    public PlayerDataCache() {
        super(Caffeine.newBuilder()
                .maximumSize(MAX_SIZE)
                .expireAfterAccess(Duration.ofMinutes(10))
                .<UUID, CachedPlayer>build());
    }
}
