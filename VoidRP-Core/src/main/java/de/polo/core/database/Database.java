package de.polo.core.database;

import de.polo.core.database.utility.Result;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Database {

    default void init() {
        setup();
    }

    void setup();

    CompletableFuture<Result> queryThreaded(String query);

    CompletableFuture<Integer> queryThreaded(String query, Object... args);

    void executeAsync(String sql);

    CompletableFuture<Optional<Integer>> queryThreadedWithGeneratedKeys(String query, Object... args);

    CompletableFuture<List<Map<String, Object>>> executeQueryAsync(String query, Object... args);

    CompletableFuture<Integer> insertAsync(String query, Object... args);

    CompletableFuture<Optional<Integer>> insertAndGetKeyAsync(String query, Object... args);

    CompletableFuture<Integer> updateAsync(String query, Object... args);

    CompletableFuture<Integer> deleteAsync(String query, Object... args);

    void close();
}