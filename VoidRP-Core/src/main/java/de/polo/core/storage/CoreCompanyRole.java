package de.polo.core.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.polo.api.company.Company;
import de.polo.api.company.CompanyRole;
import de.polo.core.Main;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class CoreCompanyRole implements CompanyRole {
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private Company company;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private List<String> permissions = new ObjectArrayList<>();

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
