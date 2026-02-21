package de.polo.core.infrastructure.persistence;

import de.polo.api.faction.Faction;
import de.polo.api.faction.repository.FactionRepository;
import de.polo.core.infrastructure.cache.CachedFaction;
import de.polo.core.infrastructure.cache.FactionCache;
import de.polo.core.utils.BetterExecutor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Cache-first implementation of {@link FactionRepository}.
 *
 * <p><b>Read path:</b> All reads are served directly from the {@link FactionCache}.
 * Faction data is loaded from the DB at startup by the existing
 * {@code FactionManager} and seeded into the cache, so cache misses on running
 * server are impossible for factions that exist.
 *
 * <p><b>Write path (bank / equip / motd / chatColor):</b> Only the in-memory
 * {@link CachedFaction} is updated and the dirty flag is set.  The
 * {@link FlushService} persists the change in the next scheduled batch
 * (every ~10 s), on server stop, or when explicitly called.
 *
 * <p><b>Why batch writes?</b> Faction bank updates happen during every job
 * payout, shop transaction, and payday for potentially 400 players.  The old
 * approach issued one {@code UPDATE factions SET bank = ?} per operation.
 * With the hybrid strategy, 400 concurrent bank changes produce a single
 * Hibernate {@code merge()} per faction per flush interval.
 */
public final class HibernateFactionRepository implements FactionRepository, Flushable {

    private final SessionFactory sessionFactory;
    private final FactionCache cache;

    public HibernateFactionRepository(SessionFactory sessionFactory, FactionCache cache) {
        this.sessionFactory = sessionFactory;
        this.cache = cache;
    }

    // ── Read helpers ────────────────────────────────────────────────────────

    @Override
    public Collection<Faction> findAll() {
        List<Faction> result = new ArrayList<>();
        for (CachedFaction cf : cache.allEntries()) {
            result.add(cf.getDomain());
        }
        return result;
    }

    @Override
    public CompletableFuture<Optional<Faction>> findById(int id) {
        CachedFaction cached = cache.get(id);
        if (cached != null) {
            return CompletableFuture.completedFuture(Optional.of(cached.getDomain()));
        }
        // Not in cache – should not normally happen after startup load.
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Optional<Faction>> findByName(String name) {
        for (CachedFaction cf : cache.allEntries()) {
            if (cf.getDomain().getName().equalsIgnoreCase(name)) {
                return CompletableFuture.completedFuture(Optional.of(cf.getDomain()));
            }
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    // ── Dirty tracking ──────────────────────────────────────────────────────

    @Override
    public void markDirty(int factionId) {
        CachedFaction cached = cache.get(factionId);
        if (cached != null) {
            cached.markDirty();
        }
    }

    // ── Flush (batch persist) ────────────────────────────────────────────────

    /**
     * Flushes all dirty {@link CachedFaction} entries to the database in a
     * single Hibernate transaction.  Dirty flags are cleared atomically
     * before opening the session; any failure re-marks only the affected
     * entries.
     */
    @Override
    public CompletableFuture<Void> flushDirty() {
        return CompletableFuture.runAsync(() -> {
            List<CachedFaction> toFlush = new ArrayList<>();
            for (CachedFaction cf : cache.allEntries()) {
                if (cf.clearDirty()) {
                    toFlush.add(cf);
                }
            }
            if (toFlush.isEmpty()) {
                return;
            }
            try (Session session = sessionFactory.openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    for (CachedFaction cf : toFlush) {
                        session.merge(cf.getEntity());
                    }
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    // Re-mark only the entries that were cleared in this attempt.
                    toFlush.forEach(CachedFaction::markDirty);
                    throw new RuntimeException("FactionFlushDirty failed", e);
                }
            }
        }, BetterExecutor.executor);
    }

    // ── Cache access ────────────────────────────────────────────────────────

    /**
     * Exposes the {@link FactionCache} so that {@code FactionManager} can
     * seed entries on startup and retrieve them later.
     */
    public FactionCache getCache() {
        return cache;
    }

    /**
     * Exposes the {@link SessionFactory} for callers that need single-entity
     * single-entity flushes (e.g. {@link FlushService#flushEntry}).
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
