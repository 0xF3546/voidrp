package de.polo.api.company;

import java.util.List;

public interface CompanyRole {
    int getId();
    void setId(int id);

    Company getCompany();
    void setCompany(Company company);

    String getName();
    void setName(String name);

    List<String> getPermissions();
    void setPermissions(List<String> permissions);

    void addPermission(String permission);
    void removePermission(String permission);
    boolean hasPermission(String permission);

    void save();
}
