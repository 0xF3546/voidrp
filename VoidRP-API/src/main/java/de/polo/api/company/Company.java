package de.polo.api.company;

import java.util.Collection;
import java.util.UUID;

public interface Company {
    int getId();
    void setId(int id);

    String getName();
    void setName(String name);

    UUID getOwner();
    void setOwner(UUID owner);

    int getBank();
    void setBank(int bank);

    void addBank(int amount);
    void removeBank(int amount);

    void addRole(CompanyRole role);
    Collection<CompanyRole> getRoles();

    void save();
    void createRole(CompanyRole role);
    void deleteRole(CompanyRole role);

}
