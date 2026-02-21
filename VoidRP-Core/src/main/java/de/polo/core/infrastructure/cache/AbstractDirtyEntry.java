package de.polo.core.infrastructure.cache;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract base that provides the {@link AtomicBoolean} dirty-tracking
 * boilerplate shared by every cached entry ({@link CachedPlayer},
 * {@link CachedFaction}, etc.).
 *
 * <p>Subclasses only need to supply {@link #getEntity()} and their own
 * domain-specific convenience mutators.
 *
 * @param <E> the type of the Hibernate entity
 */
public abstract class AbstractDirtyEntry<E> implements DirtyTrackable<E> {

    private final AtomicBoolean dirty = new AtomicBoolean(false);

    @Override
    public boolean isDirty() {
        return dirty.get();
    }

    @Override
    public void markDirty() {
        dirty.set(true);
    }

    @Override
    public boolean clearDirty() {
        return dirty.getAndSet(false);
    }
}
