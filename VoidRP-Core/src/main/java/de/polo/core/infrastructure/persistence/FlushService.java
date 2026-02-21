package de.polo.core.infrastructure.persistence;

import de.polo.core.infrastructure.cache.DirtyTrackable;
import de.polo.core.utils.BetterExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Single, generalised flush service that drives all {@link Flushable}
 * repositories (currently {@link HibernatePlayerRepository} and
 * {@link HibernateFactionRepository}).
 *
 * <h2>Flush triggers</h2>
 * <ol>
 *   <li><b>Scheduled flush</b> – every {@value #FLUSH_INTERVAL_TICKS} ticks
 *       (≈ 10 s at 20 TPS).  DB work runs on the async executor so the Bukkit
 *       main thread is never blocked.</li>
 *   <li><b>Single-entry flush</b> – call {@link #flushEntry(DirtyTrackable)}
 *       when a player quits to persist only that player's data immediately.</li>
 *   <li><b>Server stop</b> – call {@link #flushAllSync()} from
 *       {@code JavaPlugin#onDisable()} to guarantee no data is lost.</li>
 * </ol>
 *
 * <p>Previously there were two nearly identical flush services
 * ({@code FlushService} + {@code FactionFlushService}).  This single class
 * replaces both, accepting any number of {@link Flushable} repositories.
 */
public final class FlushService {

    /** Flush every 10 seconds (200 ticks at 20 TPS). */
    private static final long FLUSH_INTERVAL_TICKS = 200L;

    private final List<Flushable> repositories;
    private final SessionFactory sessionFactory;
    private final JavaPlugin plugin;

    private BukkitRunnable scheduledTask;

    /**
     * @param sessionFactory shared Hibernate session factory (used for single-entry flushes)
     * @param plugin         owning Bukkit plugin
     * @param repositories   one or more {@link Flushable} repositories to flush on schedule
     */
    public FlushService(SessionFactory sessionFactory,
                        JavaPlugin plugin,
                        Flushable... repositories) {
        this.sessionFactory = sessionFactory;
        this.plugin = plugin;
        this.repositories = Arrays.asList(repositories);
    }

    /** Starts the periodic flush scheduler. Must be called from {@code onEnable}. */
    public void start() {
        scheduledTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Flushable repo : repositories) {
                    repo.flushDirty()
                            .exceptionally(ex -> {
                                plugin.getLogger().log(Level.SEVERE,
                                        "[FlushService] Scheduled flush failed", ex);
                                return null;
                            });
                }
            }
        };
        scheduledTask.runTaskTimerAsynchronously(plugin, FLUSH_INTERVAL_TICKS, FLUSH_INTERVAL_TICKS);
    }

    /** Cancels the periodic task. Call {@link #flushAllSync()} first. */
    public void stop() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel();
        }
    }

    /**
     * Blocking flush of ALL dirty entries across every managed repository.
     * Must only be called from {@code JavaPlugin#onDisable()}.
     */
    public void flushAllSync() {
        for (Flushable repo : repositories) {
            try {
                repo.flushDirty().get();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE,
                        "[FlushService] Shutdown flush failed – some data may be lost!", e);
            }
        }
    }

    /**
     * Immediately flushes a single {@link DirtyTrackable} entry (e.g. a
     * player that just quit) without waiting for the next scheduled batch.
     *
     * <p>The dirty flag is cleared atomically before the session is opened so
     * a concurrent scheduled flush will not double-write the same entry.  On
     * failure the flag is re-set so the next scheduled flush can retry.
     *
     * @param entry the cached entry to persist
     * @param <E>   the Hibernate entity type
     */
    public <E> void flushEntry(DirtyTrackable<E> entry) {
        if (!entry.isDirty()) {
            return;
        }
        if (entry.clearDirty()) {
            CompletableFuture.runAsync(() -> {
                try (Session session = sessionFactory.openSession()) {
                    Transaction tx = session.beginTransaction();
                    try {
                        session.merge(entry.getEntity());
                        tx.commit();
                    } catch (Exception e) {
                        tx.rollback();
                        entry.markDirty();
                        plugin.getLogger().log(Level.SEVERE,
                                "[FlushService] Single entry flush failed", e);
                    }
                }
            }, BetterExecutor.executor)
                    .exceptionally(ex -> {
                        plugin.getLogger().log(Level.SEVERE,
                                "[FlushService] Single entry flush failed (outer)", ex);
                        return null;
                    });
        }
    }
}
