package de.polo.core.infrastructure.persistence;

import de.polo.core.infrastructure.cache.FactionCache;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Scheduled service that persists all dirty {@link de.polo.core.infrastructure.cache.CachedFaction}
 * entries to the database in a single batch Hibernate transaction.
 *
 * <h2>Flush triggers</h2>
 * <ol>
 *   <li><b>Scheduled flush</b> – every {@value #FLUSH_INTERVAL_TICKS} ticks
 *       (≈ 10 s at 20 TPS).  DB work runs on the async executor – the Bukkit
 *       main thread is never blocked.</li>
 *   <li><b>Server stop</b> – call {@link #flushAllSync()} from
 *       {@code JavaPlugin#onDisable()} to guarantee no data is lost.</li>
 * </ol>
 */
public final class FactionFlushService {

    /** Flush every 10 seconds (200 ticks at 20 TPS). */
    private static final long FLUSH_INTERVAL_TICKS = 200L;

    private final HibernateFactionRepository repository;
    private final FactionCache cache;
    private final JavaPlugin plugin;

    private BukkitRunnable scheduledTask;

    public FactionFlushService(HibernateFactionRepository repository,
                               FactionCache cache,
                               JavaPlugin plugin) {
        this.repository = repository;
        this.cache = cache;
        this.plugin = plugin;
    }

    /** Starts the periodic flush scheduler. Must be called from {@code onEnable}. */
    public void start() {
        scheduledTask = new BukkitRunnable() {
            @Override
            public void run() {
                repository.flushDirty()
                        .exceptionally(ex -> {
                            plugin.getLogger().log(Level.SEVERE,
                                    "[FactionFlushService] Scheduled flush failed", ex);
                            return null;
                        });
            }
        };
        scheduledTask.runTaskTimerAsynchronously(plugin, FLUSH_INTERVAL_TICKS, FLUSH_INTERVAL_TICKS);
    }

    /** Cancels the periodic task.  Call {@link #flushAllSync()} first. */
    public void stop() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel();
        }
    }

    /**
     * Blocking flush of ALL dirty entries.  Must only be called from
     * {@code JavaPlugin#onDisable()} where blocking the server thread is
     * acceptable and the Bukkit scheduler is no longer running.
     */
    public void flushAllSync() {
        try {
            repository.flushDirty().get();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "[FactionFlushService] Shutdown flush failed – faction bank data may be lost!", e);
        }
    }
}
