package de.polo.core.infrastructure.persistence;

import java.util.concurrent.CompletableFuture;

/**
 * Marks a repository as capable of batch-flushing its dirty entries to
 * the database.
 *
 * <p>Both {@link HibernatePlayerRepository} and {@link HibernateFactionRepository}
 * implement this interface, allowing the single {@link FlushService} to
 * manage all persistence flushes without duplication.
 */
public interface Flushable {

    /**
     * Persists all dirty entries in a single Hibernate transaction.
     * Implementations must atomically clear dirty flags before opening the
     * transaction and re-mark only the affected entries on rollback.
     *
     * @return a future that completes when the flush is done
     */
    CompletableFuture<Void> flushDirty();
}
