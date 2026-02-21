package de.polo.api.utils.cache;

/**
 * Base class for all entities that can be stored in a {@link GenericCache}.
 * Subclasses must provide a unique integer identifier.
 *
 * @author VoidRP
 * @version 1.0.0
 * @see GenericCache
 * @see Cache
 */
public abstract class EntityBase {

    /**
     * Returns the unique identifier of this entity.
     *
     * @return the entity id
     */
    public abstract int getId();
}
