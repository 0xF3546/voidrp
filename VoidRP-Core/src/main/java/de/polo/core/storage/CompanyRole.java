package de.polo.core.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.polo.core.Main;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class CompanyRole {
    private int id;
    private Company company;
    private String name;
    private List<String> permissions = new ObjectArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(String permission) {
        this.permissions.add(permission);
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(String permission) {
        for (String perm : permissions) {
            if (perm.equalsIgnoreCase(permission)) {
                return true;
            }
        }
        return false;
    }

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE company_roles SET name = ?, permissions = ? WHERE id = ?");
        statement.setString(1, getName());
        ObjectMapper mapper = new ObjectMapper();
        String permissionsJson = mapper.writeValueAsString(permissions);
        statement.setString(2, permissionsJson);
        statement.setInt(3, getId());
        statement.execute();
        statement.close();
        connection.close();
    }
}
