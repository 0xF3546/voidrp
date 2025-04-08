package de.polo.api;

import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Server {
    /**
     * Retrieves a Spring-managed bean of the specified type from the application context.
     *
     * <p>This method allows the server to access beans managed by the Spring
     * Framework, enabling integration with various Spring components and services.
     * The type of the bean is specified by the {@code clazz} parameter, and the method
     * returns the bean instance if it is available in the context.</p>
     *
     * <p>This method is especially useful for accessing shared services, utilities, or
     * other components that are defined in the Spring context but are required by the
     * server at runtime.</p>
     *
     * @param <T>   the type of the bean to be retrieved.
     * @param clazz the {@link Class} object representing the type of the bean.
     * @return the Spring-managed bean of the specified type.
     */
    <T> T getBean(@NotNull final Class<T> clazz);
}
