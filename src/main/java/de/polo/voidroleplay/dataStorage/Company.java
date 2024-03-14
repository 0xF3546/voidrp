package de.polo.voidroleplay.dataStorage;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.polo.voidroleplay.Main;
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
    private int id;
    private String name;
    private UUID owner;
    private int bank;

    private List<CompanyRole> roles = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public int getBank() {
        return bank;
    }

    public void setBank(int bank) {
        this.bank = bank;
    }

    public void addRole(CompanyRole role) {
        roles.add(role);
    }

    public Collection<CompanyRole> getRoles() {
        return roles;
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
