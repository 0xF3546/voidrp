package dev.vansen.singleline;

import de.polo.voidroleplay.utils.BetterExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * A powerful single-line utility for executing, transforming, and conditionally handling
 * values with various conditions and actions. Includes extensive functionality for a
 * streamlined chaining experience.
 *
 * @param <T> The type of the main value.
 */
@SuppressWarnings("unused")
public class SingleLine<T> {
    private T value;
    private boolean conditionMet = true;
    private final AtomicInteger counter = new AtomicInteger(1);

    /**
     * Creates a new SingleLine instance with the given initial value.
     */
    public SingleLine(T value) {
        this.value = value;
    }

    /**
     * Starts a new SingleLine instance with the given initial value.
     */
    public static <T> SingleLine<T> from(@NotNull T initial) {
        return new SingleLine<>(initial);
    }

    /**
     * Starts a new SingleLine instance with a null value.
     */
    public static <T> SingleLine<T> from() {
        return new SingleLine<>(null);
    }

    /**
     * Starts a new SingleLine instance with a null value.
     */
    public static <T> SingleLine<T> empty() {
        return new SingleLine<>(null);
    }

    /**
     * Sets the main value for the SingleLine instance.
     */
    public SingleLine<T> value(@NotNull T value) {
        this.value = value;
        return this;
    }

    /**
     * Executes the action if the current value meets the condition.
     */
    public SingleLine<T> ifCondition(@NotNull Predicate<T> condition) {
        debugDefaultIf();
        conditionMet = conditionMet && (value != null && condition.test(value));
        return this;
    }

    /**
     * Executes the action if the given condition is false.
     */
    public SingleLine<T> unlessCondition(@NotNull Predicate<T> condition) {
        debugDefaultIf();
        conditionMet = conditionMet && (value == null || !condition.test(value));
        return this;
    }

    /**
     * Terminates further processing if this condition is met.
     */
    public SingleLine<T> terminateIf(@NotNull Predicate<T> condition) {
        debugDefaultIf();
        if (condition.test(value)) {
            conditionMet = false;
        }
        return this;
    }

    /**
     * Applies a transformation function to the value and returns a new SingleLine instance.
     */
    public <R> SingleLine<R> map(@NotNull Function<T, R> transformer) {
        debugDefaultIf();
        return conditionMet && value != null ? new SingleLine<>(transformer.apply(value)) : new SingleLine<>(null);
    }

    /**
     * Executes an action on the value if all conditions are met.
     */
    public SingleLine<T> execute(@NotNull Consumer<T> action) {
        debugDefaultIf();
        if (conditionMet && value != null) {
            action.accept(value);
        }
        return this;
    }

    /**
     * Executes an action if the value is null.
     */
    public SingleLine<T> ifNull(@NotNull Consumer<Void> action) {
        debugDefaultIf();
        if (value == null) {
            action.accept(null);
        }
        return this;
    }

    /**
     * Executes an action if the value is not null.
     */
    public SingleLine<T> ifNotNull(@NotNull Consumer<T> action) {
        debugDefaultIf();
        if (value != null) {
            action.accept(value);
        }
        return this;
    }

    /**
     * Sets a default value if the current value is null.
     */
    public SingleLine<T> defaultIfNull(@NotNull T defaultValue) {
        debugDefaultIf();
        if (value == null) {
            value = defaultValue;
        }
        return this;
    }

    /**
     * Switches to a different value and resets condition state.
     */
    public <R> SingleLine<R> another(@Nullable R newValue) {
        return new SingleLine<>(newValue);
    }

    /**
     * Returns a specific value if the condition is met.
     */
    public SingleLine<T> returnIf(@NotNull Predicate<T> condition, @NotNull T returnValue) {
        debugDefaultIf();
        if (condition.test(value)) {
            value = returnValue;
        }
        return this;
    }

    /**
     * Executes a Runnable.
     */
    public SingleLine<T> execute(@NotNull Runnable runnable) {
        debugDefaultIf();
        runnable.run();
        return this;
    }

    /**
     * Executes a Runnable asynchronously.
     */
    public SingleLine<T> async(@NotNull Runnable runnable) {
        debugDefaultIf();
        CompletableFuture.runAsync(runnable, BetterExecutor.executor);
        return this;
    }

    /**
     * Executes a Runnable asynchronously and waits for it to finish.
     */
    public SingleLine<T> asyncWait(@NotNull Runnable runnable) {
        debugDefaultIf();
        CompletableFuture.runAsync(runnable, BetterExecutor.executor).join();
        return this;
    }

    /**
     * Executes a Runnable asynchronously.
     */
    public SingleLine<T> async(@NotNull Runnable runnable, @NotNull Executor executor) {
        debugDefaultIf();
        CompletableFuture.runAsync(runnable, executor);
        return this;
    }

    /**
     * Executes a Runnable asynchronously and waits for it to finish.
     */
    public SingleLine<T> asyncWait(@NotNull Runnable runnable, @NotNull Executor executor) {
        debugDefaultIf();
        CompletableFuture.runAsync(runnable, executor).join();
        return this;
    }

    /**
     * Executes a Consumer asynchronously.
     */
    public SingleLine<T> async(@NotNull Consumer<T> consumer) {
        debugDefaultIf();
        CompletableFuture.runAsync(() -> consumer.accept(value), BetterExecutor.executor);
        return this;
    }

    /**
     * Executes a Consumer asynchronously and waits for it to finish.
     */
    public SingleLine<T> asyncWait(@NotNull Consumer<T> consumer) {
        debugDefaultIf();
        CompletableFuture.runAsync(() -> consumer.accept(value), BetterExecutor.executor).join();
        return this;
    }

    /**
     * Executes a Consumer asynchronously.
     */
    public SingleLine<T> async(@NotNull Consumer<T> consumer, @NotNull Executor executor) {
        debugDefaultIf();
        CompletableFuture.runAsync(() -> consumer.accept(value), executor);
        return this;
    }

    /**
     * Executes a Consumer asynchronously and waits for it to finish.
     */
    public SingleLine<T> asyncWait(@NotNull Consumer<T> consumer, @NotNull Executor executor) {
        debugDefaultIf();
        CompletableFuture.runAsync(() -> consumer.accept(value), executor).join();
        return this;
    }

    /**
     * Returns a transformed value if the condition is met.
     */
    public SingleLine<T> returnIf(@NotNull Predicate<T> condition, @NotNull Function<T, T> returnFunc) {
        debugDefaultIf();
        if (condition.test(value) && value != null) {
            value = returnFunc.apply(value);
        }
        return this;
    }

    /**
     * Provides a fallback value if the current value is null.
     */
    public SingleLine<T> orElse(@NotNull T fallback) {
        debugDefaultIf();
        if (value == null) {
            value = fallback;
        }
        return this;
    }

    /**
     * Wraps the value in an Optional
     */
    public SingleLine<Optional<T>> wrapOptional() {
        debugDefaultIf();
        return new SingleLine<>(Optional.ofNullable(value));
    }

    /**
     * Returns a transformed value and terminates if conditions were not met.
     */
    public SingleLine<T> returning(@NotNull Function<T, T> function) {
        debugDefaultIf();
        if (conditionMet && value != null) {
            value = function.apply(value);
        }
        return this;
    }

    /**
     * Logs the current value for debugging purposes.
     */
    public SingleLine<T> debug(@NotNull Consumer<T> logger) {
        logger.accept(value);
        return this;
    }

    /**
     * Logs the current integer for debugging purposes.
     */
    public SingleLine<T> debugDefault() {
        if (SingleLineOptions.USE_COMPONENT_LOGGER.enabled()) {
            try {
                Class.forName("ComponentLogger")
                        .getMethod("logger", String.class)
                        .invoke(null, "SingleLine")
                        .getClass()
                        .getMethod("info", Object.class)
                        .invoke(Class.forName("ComponentLogger")
                                .getMethod("logger", String.class)
                                .invoke(null, "SingleLine"), counter.getAndIncrement());
            } catch (Exception e) {
                System.out.println("Not running on paper! disabling component logger");
                SingleLineOptions.USE_COMPONENT_LOGGER.enabled(false);
            }
        } else if (SingleLineOptions.USE_NORMAL_LOGGER_INSTEAD_OF_PRINT.enabled())
            Logger.getLogger("SingleLine").info(String.valueOf(counter.getAndIncrement()));
        else System.out.println(counter.getAndIncrement());
        return this;
    }

    /**
     * Logs the current integer if the condition is met.
     */
    public SingleLine<T> debugDefaultIf(@NotNull Predicate<T> condition) {
        if (condition.test(value)) {
            if (SingleLineOptions.USE_COMPONENT_LOGGER.enabled()) {
                try {
                    Class.forName("ComponentLogger")
                            .getMethod("logger", String.class)
                            .invoke(null, "SingleLine")
                            .getClass()
                            .getMethod("info", Object.class)
                            .invoke(Class.forName("ComponentLogger")
                                    .getMethod("logger", String.class)
                                    .invoke(null, "SingleLine"), counter.getAndIncrement());
                } catch (Exception e) {
                    System.out.println("Not running on paper! disabling component logger, the debug statements might not work");
                    SingleLineOptions.USE_COMPONENT_LOGGER.enabled(false);
                }
            } else if (SingleLineOptions.USE_NORMAL_LOGGER_INSTEAD_OF_PRINT.enabled())
                Logger.getLogger("SingleLine").info(String.valueOf(counter.getAndIncrement()));
            else System.out.println(counter.getAndIncrement());
        }
        return this;
    }

    /**
     * Logs the current integer if the debug is enabled.
     */
    @SuppressWarnings("UnusedReturnValue")
    public SingleLine<T> debugDefaultIf() {
        if (SingleLineOptions.DEBUG.enabled()) {
            if (SingleLineOptions.USE_COMPONENT_LOGGER.enabled()) {
                try {
                    Class.forName("ComponentLogger")
                            .getMethod("logger", String.class)
                            .invoke(null, "SingleLine")
                            .getClass()
                            .getMethod("info", Object.class)
                            .invoke(Class.forName("ComponentLogger")
                                    .getMethod("logger", String.class)
                                    .invoke(null, "SingleLine"), counter.getAndIncrement());
                } catch (Exception e) {
                    System.out.println("Not running on paper! disabling component logger");
                    SingleLineOptions.USE_COMPONENT_LOGGER.enabled(false);
                }
            } else if (SingleLineOptions.USE_NORMAL_LOGGER_INSTEAD_OF_PRINT.enabled())
                Logger.getLogger("SingleLine").info(String.valueOf(counter.getAndIncrement()));
            else System.out.println(counter.getAndIncrement());
        }
        return this;
    }

    /**
     * Retrieves the final value.
     */
    @Nullable
    public T get() {
        return value;
    }
}