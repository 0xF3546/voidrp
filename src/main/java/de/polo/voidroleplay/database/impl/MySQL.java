package de.polo.voidroleplay.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.polo.voidroleplay.database.Database;
import de.polo.voidroleplay.utils.BetterExecutor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MySQL implements Database {
    static final String url = "jdbc:mysql://185.117.3.65/minecraft?autoReconnect=true&useSSL=false";
    static String user = null;
    static String password = null;
    private HikariDataSource dataSource;

    public MySQL() {
        init();
    }

    @Override
    @SneakyThrows(IOException.class)
    public void setup() {
        File file = new File("plugins//roleplay//database.yml");
        if (!file.exists()) {
            file.createNewFile();
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File("plugins//roleplay//database.yml"));
            cfg.set("password", "Datenbank-Passwort");
            cfg.set("user", "Datenbank-Benutzer");
            cfg.save(file);
            return;
        }
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(yaml.getString("user"));
        config.setPassword(yaml.getString("password"));
        config.setMaximumPoolSize(10000);
        config.setIdleTimeout(30000);

        dataSource = new HikariDataSource(config);
    }

    @Override
    public void close() {
        dataSource.close();
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public Statement getStatement() throws SQLException {
        Connection connection = getConnection();
        return connection.createStatement();
    }

    @Override
    public CompletableFuture<ResultSet> queryThreaded(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Statement statement = getStatement();
                return statement.executeQuery(query);
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
                    return results;
                }
            } catch (SQLException e) {
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

                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedKey = generatedKeys.getInt(1);
                        return Optional.of(generatedKey);
                    }
                }
                return Optional.empty();
            } catch (SQLException e) {
                throw new RuntimeException("Database insert failed", e);
            }
        }, BetterExecutor.executor);
    }

    @Override
    public CompletableFuture<Integer> updateAsync(String query, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }
                return statement.executeUpdate();
            } catch (SQLException e) {
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

    public interface forum {
        String url = "jdbc:mysql://185.117.3.65/wcf?autoReconnect=true&useSSL=false";

        static Statement getStatement() throws SQLException {
            Connection connection = DriverManager.getConnection(url, user, password);
            return connection.createStatement();
        }
    }
}
