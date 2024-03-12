package de.polo.voidroleplay.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.Company;
import de.polo.voidroleplay.dataStorage.CompanyRole;
import de.polo.voidroleplay.database.MySQL;
import lombok.SneakyThrows;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CompanyManager {
    private final List<Company> companies = new ArrayList<>();
    private final MySQL mySQL;
    public CompanyManager(MySQL mySQL) {
        this.mySQL = mySQL;
        init();
    }

    @SneakyThrows
    private void init() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM companies");
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            Company company = new Company();
            company.setId(result.getInt("id"));
            company.setOwner(UUID.fromString(result.getString("owner")));
            company.setBank(result.getInt("bank"));
            company.setName(result.getString("name"));
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM company_roles WHERE companyId = ?");
            preparedStatement.setInt(1, result.getInt("id"));
            ResultSet roleResult = preparedStatement.executeQuery();
            while (roleResult.next()) {
                CompanyRole role = new CompanyRole();
                role.setCompany(company);
                role.setName(roleResult.getString("name"));
                String permissionsJson = roleResult.getString("permissions");
                ObjectMapper mapper = new ObjectMapper();
                try {
                    List<String> permissions = mapper.readValue(permissionsJson, new TypeReference<List<String>>() {});
                    role.setPermissions(permissions);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            companies.add(company);
        }
    }

    @SneakyThrows
    public boolean create(Company company) {
        for (Company c : companies) {
            if (c.getName() == null) continue;
            if (c.getName().equalsIgnoreCase(company.getName())) {
                return false;
            }
        }
        Connection connection = mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO companies (name, owner) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, company.getName());
        statement.setString(2, company.getOwner().toString());
        statement.execute();
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            int key = generatedKeys.getInt(1);
            company.setId(key);
        }
        companies.add(company);
        statement.close();
        connection.close();
        return true;
    }

    @SneakyThrows
    public boolean delete(Company company) {
        for (Company c : companies) {
            if (c.getName().equalsIgnoreCase(company.getName())) {
                return false;
            }
        }
        Connection connection = mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("DELETE FROM companies WHERE id = ?");
        statement.setInt(1, company.getId());
        statement.execute();
        companies.remove(company);
        statement.close();
        connection.close();
        return true;
    }

    public Company getCompanyById(int id) {
        for (Company c : companies) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }
}
