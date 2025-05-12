package de.polo.core.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.polo.core.Main;
import de.polo.core.database.impl.CoreDatabase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.CoreCompany;
import de.polo.core.storage.CoreCompanyRole;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.UUID;

public class CompanyManager {
    private final List<CoreCompany> companies = new ObjectArrayList<>();
    private final CoreDatabase coreDatabase;

    public CompanyManager(CoreDatabase coreDatabase) {
        this.coreDatabase = coreDatabase;
        init();
    }

    @SneakyThrows
    private void init() {
        // Versuche, die Verbindung und die PreparedStatement Objekte zu verwalten
        try (Connection connection = Main.getInstance().coreDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM companies");
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                CoreCompany coreCompany = new CoreCompany();
                coreCompany.setId(result.getInt("id"));
                coreCompany.setOwner(UUID.fromString(result.getString("owner")));
                coreCompany.setBank(result.getInt("bank"));
                coreCompany.setName(result.getString("name"));

                // Rollen für die Company abfragen
                try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM company_roles WHERE companyId = ?")) {
                    preparedStatement.setInt(1, coreCompany.getId()); // Setze den Parameter vor der Abfrage

                    try (ResultSet roleResult = preparedStatement.executeQuery()) {
                        while (roleResult.next()) {
                            CoreCompanyRole role = new CoreCompanyRole();
                            role.setCompany(coreCompany);
                            role.setName(roleResult.getString("name"));
                            role.setId(roleResult.getInt("id"));
                            String permissionsJson = roleResult.getString("permissions");
                            ObjectMapper mapper = new ObjectMapper();
                            try {
                                List<String> permissions = mapper.readValue(permissionsJson, new TypeReference<List<String>>() {
                                });
                                role.setPermissions(permissions);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            coreCompany.addRole(role);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace(); // Fehlerbehandlung für die ResultSet-Schleife
                    }
                } catch (SQLException e) {
                    e.printStackTrace(); // Fehlerbehandlung für PreparedStatement
                }

                companies.add(coreCompany);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Fehlerbehandlung für die Hauptabfrage
        }
    }

    @SneakyThrows
    public boolean create(CoreCompany coreCompany) {
        for (CoreCompany c : companies) {
            if (c.getName() == null) continue;
            if (c.getName().equalsIgnoreCase(coreCompany.getName())) {
                return false;
            }
        }
        Connection connection = coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO companies (name, owner) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, coreCompany.getName());
        statement.setString(2, coreCompany.getOwner().toString());
        statement.execute();
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            int key = generatedKeys.getInt(1);
            coreCompany.setId(key);
        }
        companies.add(coreCompany);
        statement.close();
        connection.close();
        return true;
    }

    @SneakyThrows
    public boolean delete(CoreCompany coreCompany) {
        for (CoreCompany c : companies) {
            if (c.getName().equalsIgnoreCase(coreCompany.getName())) {
                return false;
            }
        }
        Connection connection = coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("DELETE FROM companies WHERE id = ?");
        statement.setInt(1, coreCompany.getId());
        statement.execute();
        companies.remove(coreCompany);
        statement.close();
        connection.close();
        return true;
    }

    public CoreCompany getCompanyById(int id) {
        for (CoreCompany c : companies) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public CoreCompanyRole getCompanyRoleById(int id) {
        for (CoreCompany c : companies) {
            for (CoreCompanyRole r : c.getRoles()) {
                if (r.getId() == id) {
                    return r;
                }
            }
        }
        return null;
    }

    public void sendCompanyMessage(CoreCompany coreCompany, String message) {
        for (PlayerData playerData : Main.playerManager.getPlayers()) {
            if (playerData.getCompany().equals(coreCompany)) {
                playerData.getPlayer().sendMessage(message);
            }
        }
    }

    @SneakyThrows
    public void setPlayerRole(PlayerData playerData, CoreCompanyRole role) {
        playerData.setCompanyRole(role);
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE players SET companyRole = ? WHERE uuid = ?");
        statement.setInt(1, role.getId());
        statement.setString(2, playerData.getUuid().toString());
        statement.execute();
        statement.close();
        connection.close();
    }
}
