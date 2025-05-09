package de.polo.core.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.polo.core.database.Database;
import de.polo.core.database.utility.Result;
import de.polo.core.utils.BetterExecutor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CoreDatabase implements Database {
    static String user = null;
    static String password = null;
    private HikariDataSource dataSource;

    public CoreDatabase() {
        init();
    }

    @Override
    @SneakyThrows(IOException.class)
    public void setup() {
        File folder = new File("plugins/roleplay");
        if (!folder.exists()) {
            folder.mkdirs(); // erstellt den Ordner (auch verschachtelt)
        }

        File file = new File(folder, "database.yml");

        if (!file.exists()) {
            file.createNewFile();

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("password", "Datenbank-Passwort");
            config.set("user", "Datenbank-Benutzer");
            config.set("host", "Datenbank-Host");
            config.set("table", "Datenbank-Name");
            config.save(file);
            return;
        }

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        HikariConfig hikariConfig = new HikariConfig();
        System.out.println("Connecting to database...");
        System.out.println("Host: " + yaml.getString("host"));
        System.out.println("User: " + yaml.getString("user"));
        System.out.println("Table: " + yaml.getString("table"));
        hikariConfig.setJdbcUrl("jdbc:mysql://" + yaml.getString("host") + "/" + yaml.getString("table") + "?autoReconnect=true&useSSL=false");
        hikariConfig.setUsername(yaml.getString("user"));
        hikariConfig.setPassword(yaml.getString("password"));
        hikariConfig.setMaximumPoolSize(100000); // 100000 war extrem hoch
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setLeakDetectionThreshold(10000); // Warnung, wenn Verbindung >10 Sek offen
        hikariConfig.setIdleTimeout(10000);

        dataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void close() {
        dataSource.close();
    }

    @SneakyThrows
    public Connection getConnection() {
        return dataSource.getConnection();
    }

    public Statement getStatement() throws SQLException {
        Connection connection = getConnection();
        return connection.createStatement();
    }

    @Override
    public CompletableFuture<Result> queryThreaded(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Statement statement = getStatement();
                return Result.of(statement.executeQuery(query), statement);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Override
    public CompletableFuture<Integer> queryThreaded(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }
                return statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, BetterExecutor.executor);
    }

    // Easier refactoring.
    @Override
    public void executeAsync(String sql) {
        CompletableFuture.runAsync(() -> {
            try (Statement statement = getStatement()) {
                statement.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, BetterExecutor.executor);
    }

    @Override
    public CompletableFuture<Optional<Integer>> queryThreadedWithGeneratedKeys(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                statement.executeUpdate();

                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return Optional.of(resultSet.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error executing query", e);
            }
            return Optional.empty();
        }, BetterExecutor.executor);
    }

    @Override
    public CompletableFuture<List<Map<String, Object>>> executeQueryAsync(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Executing query: " + query + " with args: " + Arrays.toString(args));
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Map<String, Object>> results = new ObjectArrayList<>();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (resultSet.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                        }
                        results.add(row);
                    }
                    System.out.println("Query successful, results: " + results);
                    return results;
                }
            } catch (SQLException e) {
                System.err.println("SQL Exception: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }, BetterExecutor.executor);
    }

    @Override
    public CompletableFuture<Integer> insertAsync(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }
                return statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Database insert failed", e);
            }
        }, BetterExecutor.executor);
    }

    @Override
    public CompletableFuture<Optional<Integer>> insertAndGetKeyAsync(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Insert failed, no rows affected.");
                }

                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return Optional.of(resultSet.getInt(1));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Error executing query", e);
            }
            return Optional.empty();
        }, BetterExecutor.executor);
    }

    @Override
    public CompletableFuture<Integer> updateAsync(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Executing update query: " + query);
            System.out.println("With arguments: " + Arrays.toString(args));

            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                int rowsAffected = statement.executeUpdate();
                System.out.println("Update successful, rows affected: " + rowsAffected);

                return rowsAffected;
            } catch (SQLException e) {
                System.err.println("Database update failed: " + e.getMessage());
                e.printStackTrace();

                throw new RuntimeException("Database update failed", e);
            }
        }, BetterExecutor.executor);
    }

    @Override
    public CompletableFuture<Integer> deleteAsync(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }
                return statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Database delete failed", e);
            }
        }, BetterExecutor.executor);
    }

    public <R> CompletableFuture<R> executeQueryAsync(String sql, Function<ResultSet, R> handler, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    return handler.apply(resultSet);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Query execution failed", e);
            }
        }, BetterExecutor.executor);
    }


    public interface forum {
        String url = "jdbc:mysql://185.117.3.65/wcf?autoReconnect=true&useSSL=false";

        static Statement getStatement() throws SQLException {
            Connection connection = DriverManager.getConnection(url, user, password);
            return connection.createStatement();
        }
    }
}
