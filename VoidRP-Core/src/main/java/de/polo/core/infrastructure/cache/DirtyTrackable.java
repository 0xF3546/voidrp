package de.polo.core.infrastructure.cache;

/**
 * Common contract for any in-memory entry that tracks whether it has
 * unsaved changes that must be flushed to the database.
 *
 * @param <E> the type of the Hibernate entity this entry wraps
 */
public interface DirtyTrackable<E> {

    /** Returns the underlying Hibernate entity. */
    E getEntity();

    /** Returns {@code true} if this entry has unsaved changes. */
    boolean isDirty();

    /** Marks this entry as needing a database flush. */
    void markDirty();

    /**
     * Atomically clears the dirty flag.
     *
     * @return the previous value – {@code true} if this entry was dirty and
     *         should be included in the current flush batch
     */
    boolean clearDirty();
}
