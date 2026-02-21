package de.polo.core.infrastructure.persistence;

import de.polo.api.player.repository.PlayerRepository;
import de.polo.core.infrastructure.cache.CachedPlayer;
import de.polo.core.infrastructure.cache.PlayerDataCache;
import de.polo.core.utils.BetterExecutor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Cache-first implementation of {@link PlayerRepository}.
 *
 * <p><b>Read path:</b> Check Caffeine cache → on miss, query DB via Hibernate
 * on the async executor, store result in cache, return.
 *
 * <p><b>Write path (money / coins / crypto):</b> Only update the in-memory
 * {@link CachedPlayer} and set the dirty flag. The {@link FlushService} will
 * persist the change in the next scheduled batch (every ~10 s), on player
 * quit, or on server stop.
 *
 * <p><b>What this solves:</b> With 400 players and frequent money updates
 * (jobs, shops, payday, transfers) the old approach generated one
 * {@code UPDATE} per operation – potentially thousands per second.
 * The hybrid strategy reduces that to one batch {@code UPDATE} per dirty
 * player per flush interval.
 */
public final class HibernatePlayerRepository implements PlayerRepository, Flushable {

    private final SessionFactory sessionFactory;
    private final PlayerDataCache cache;

    public HibernatePlayerRepository(SessionFactory sessionFactory, PlayerDataCache cache) {
        this.sessionFactory = sessionFactory;
        this.cache = cache;
    }

    // ── Read helpers ────────────────────────────────────────────────────────

    @Override
    public CompletableFuture<Integer> findBargeld(UUID uuid) {
        CachedPlayer cached = cache.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached.getBargeld());
        }
        return loadFromDb(uuid).thenApply(entity -> entity != null ? entity.getBargeld() : 0);
    }

    @Override
    public CompletableFuture<Integer> findBank(UUID uuid) {
        CachedPlayer cached = cache.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached.getBank());
        }
        return loadFromDb(uuid).thenApply(entity -> entity != null ? entity.getBank() : 0);
    }

    // ── Dirty tracking ──────────────────────────────────────────────────────

    @Override
    public void markDirty(UUID uuid) {
        CachedPlayer cached = cache.get(uuid);
        if (cached != null) {
            cached.markDirty();
        }
    }

    // ── Flush (batch persist) ────────────────────────────────────────────────

    /**
     * Flushes all dirty {@link CachedPlayer} entries to the database in a
     * single Hibernate transaction.  Only players whose dirty flag is set are
     * included; the flag is cleared atomically so a concurrent flush cannot
     * double-write the same entry.
     */
    @Override
    public CompletableFuture<Void> flushDirty() {
        return CompletableFuture.runAsync(() -> {
            // Collect and clear dirty entries atomically before opening the session.
            java.util.List<CachedPlayer> toFlush = new java.util.ArrayList<>();
            for (CachedPlayer cp : cache.allEntries()) {
                if (cp.clearDirty()) {
                    toFlush.add(cp);
                }
            }
            if (toFlush.isEmpty()) {
                return;
            }
            try (Session session = sessionFactory.openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    for (CachedPlayer cp : toFlush) {
                        session.merge(cp.getEntity());
                    }
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    // Re-mark only the entries that were cleared in this attempt.
                    toFlush.forEach(CachedPlayer::markDirty);
                    throw new RuntimeException("FlushDirty failed", e);
                }
            }
        }, BetterExecutor.executor);
    }

    // ── Internal DB load ────────────────────────────────────────────────────

    /**
     * Loads a {@link PlayerEntity} from the database by UUID, stores it in
     * the cache, and returns it.  Returns {@code null} if no row is found.
     */
    public CompletableFuture<PlayerEntity> loadFromDb(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Session session = sessionFactory.openSession()) {
                PlayerEntity entity = session
                        .createQuery(
                                "FROM PlayerEntity p WHERE p.uuid = :uuid",
                                PlayerEntity.class)
                        .setParameter("uuid", uuid.toString())
                        .setMaxResults(1)
                        .uniqueResult();

                if (entity != null) {
                    cache.put(uuid, new CachedPlayer(entity));
                }
                return entity;
            }
        }, BetterExecutor.executor);
    }

    /**
     * Exposes the underlying {@link PlayerDataCache} so that callers (e.g.
     * {@link de.polo.core.player.entities.PlayerData}) can update cached
     * balances without going through the repository interface.
     */
    public PlayerDataCache getCache() {
        return cache;
    }

    /**
     * Exposes the {@link SessionFactory} for callers that need to perform
     * single-entity flushes (e.g. {@link FlushService#flushEntry}).
     */
    public org.hibernate.SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
