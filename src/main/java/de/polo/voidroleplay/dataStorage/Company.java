package de.polo.voidroleplay.dataStorage;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.polo.voidroleplay.Main;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Company {
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private UUID owner;
    @Getter
    @Setter
    private int bank;

    private final List<CompanyRole> roles = new ArrayList<>();

    public void addRole(CompanyRole role) {
        roles.add(role);
    }

    public Collection<CompanyRole> getRoles() {
        return roles;
    }

    public void addBank(int amount) {
        bank += amount;
        save();
    }

    public void removeBank(int amount) {
        bank -= amount;
        save();
    }

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE companies SET bank = ?, name = ?, owner = ? WHERE id = ?");
        statement.setInt(1, getBank());
        statement.setString(2, getName());
        statement.setString(3, getOwner().toString());
        statement.setInt(4, getId());
        statement.execute();
        statement.close();
        connection.close();
    }

    @SneakyThrows
    public void createRole(CompanyRole role) {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO company_roles (companyId, name, permissions) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setInt(1, getId());
        statement.setString(2, role.getName());
        ObjectMapper mapper = new ObjectMapper();
        String permissionsJson = mapper.writeValueAsString(role.getPermissions());
        statement.setString(3, permissionsJson);
        statement.execute();
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            int key = generatedKeys.getInt(1);
            role.setId(key);
        }
        roles.add(role);
        statement.close();
        connection.close();
    }

    @SneakyThrows
    public void deleteRole(CompanyRole role) {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("DELETE FROM company_roles WHERE id = ?");
        statement.setInt(1, role.getId());
        statement.execute();
        roles.remove(role);
        statement.close();
        connection.close();
    }
}
