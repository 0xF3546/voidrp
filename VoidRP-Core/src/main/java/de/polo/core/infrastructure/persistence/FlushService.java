package de.polo.core.infrastructure.persistence;

import de.polo.core.infrastructure.cache.CachedPlayer;
import de.polo.core.infrastructure.cache.PlayerDataCache;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Scheduled service that persists all dirty {@link CachedPlayer} entries
 * to the database in a single batch transaction.
 *
 * <h2>Flush triggers</h2>
 * <ol>
 *   <li><b>Scheduled flush</b> – every {@value #FLUSH_INTERVAL_TICKS} ticks
 *       (≈ 10 s at 20 TPS).  The actual DB work runs on the async executor so
 *       the Bukkit main thread is never blocked.</li>
 *   <li><b>Player quit</b> – call {@link #flushPlayer(CachedPlayer)} from the
 *       quit listener before removing the entry from the cache.</li>
 *   <li><b>Server stop</b> – call {@link #flushAllSync()} from
 *       {@code JavaPlugin#onDisable()} to guarantee no data is lost on
 *       shutdown.</li>
 * </ol>
 *
 * <h2>Why this is optimal for 400 players</h2>
 * <ul>
 *   <li>Money operations (jobs, shops, payday, transfers) each call
 *       {@link HibernatePlayerRepository#markDirty(java.util.UUID)} which is
 *       an O(1) {@link java.util.concurrent.atomic.AtomicBoolean} CAS – zero
 *       DB overhead.</li>
 *   <li>A single batch transaction for 400 dirty rows costs roughly the same
 *       as 2–3 individual updates due to connection/commit overhead.</li>
 *   <li>Crash safety is bounded to at most one flush interval (10 s) of
 *       money data – acceptable for an RP server where admins can compensate.</li>
 * </ul>
 */
public final class FlushService {

    /** Flush every 10 seconds (200 ticks at 20 TPS). */
    private static final long FLUSH_INTERVAL_TICKS = 200L;

    private final HibernatePlayerRepository repository;
    private final PlayerDataCache cache;
    private final JavaPlugin plugin;

    private BukkitRunnable scheduledTask;

    public FlushService(HibernatePlayerRepository repository,
                        PlayerDataCache cache,
                        JavaPlugin plugin) {
        this.repository = repository;
        this.cache = cache;
        this.plugin = plugin;
    }

    /** Starts the periodic flush scheduler.  Must be called from {@code onEnable}. */
    public void start() {
        scheduledTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Schedule the actual DB work off the main thread.
                repository.flushDirty()
                        .exceptionally(ex -> {
                            plugin.getLogger().log(Level.SEVERE,
                                    "[FlushService] Scheduled flush failed", ex);
                            return null;
                        });
            }
        };
        scheduledTask.runTaskTimerAsynchronously(plugin, FLUSH_INTERVAL_TICKS, FLUSH_INTERVAL_TICKS);
    }

    /** Cancels the periodic task.  Does NOT flush – call {@link #flushAllSync()} first. */
    public void stop() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel();
        }
    }

    /**
     * Immediately flushes a single player's data to the database.
     * Intended for use in the PlayerQuit handler before removing the entry from cache.
     */
    public void flushPlayer(CachedPlayer cachedPlayer) {
        if (!cachedPlayer.isDirty()) {
            return;
        }
        // clearDirty() atomically resets the flag so the scheduled flush
        // won't double-write this entry.
        if (cachedPlayer.clearDirty()) {
            CompletableFuture.runAsync(() -> {
                try (org.hibernate.Session session = repository.getSessionFactory().openSession()) {
                    org.hibernate.Transaction tx = session.beginTransaction();
                    try {
                        session.merge(cachedPlayer.getEntity());
                        tx.commit();
                    } catch (Exception e) {
                        tx.rollback();
                        // Re-mark so it will be caught by the next scheduled flush.
                        cachedPlayer.markDirty();
                        plugin.getLogger().log(Level.SEVERE,
                                "[FlushService] Player quit flush failed", e);
                    }
                }
            }, de.polo.core.utils.BetterExecutor.executor)
                    .exceptionally(ex -> {
                        plugin.getLogger().log(Level.SEVERE,
                                "[FlushService] Player quit flush failed (outer)", ex);
                        return null;
                    });
        }
    }

    /**
     * Blocking flush of ALL dirty entries.  Must only be called from
     * {@code JavaPlugin#onDisable()} where blocking is acceptable and the
     * Bukkit scheduler is no longer available.
     */
    public void flushAllSync() {
        try {
            repository.flushDirty().get();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "[FlushService] Shutdown flush failed – some player data may be lost!", e);
        }
    }
}
