package de.polo.core.storage;

import de.polo.core.Main;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class GasStationData {
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private int x;
    @Getter
    @Setter
    private int y;
    @Getter
    @Setter
    private int z;
    @Getter
    @Setter
    private World welt;
    @Getter
    @Setter
    private float yaw;
    @Getter
    @Setter
    private float pitch;
    @Getter
    @Setter
    private int price;
    @Getter
    @Setter
    private int literprice;
    @Getter
    @Setter
    private int liter;
    private int company;
    @Getter
    @Setter
    private int bank;

    public CoreCompany getCompany() {
        return Main.companyManager.getCompanyById(company);
    }

    public void setCompany(int company) {
        this.company = company;
    }

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE gasstations SET price = ?, literprice = ?, liter = ?, company = ? WHERE id = ?");
        statement.setInt(1, getPrice());
        statement.setInt(2, getLiterprice());
        statement.setInt(3, getLiter());
        statement.setInt(4, getCompany().getId());
        statement.setInt(5, getId());
        statement.execute();
        statement.close();
        connection.close();
    }

    public void addLiter(int liter) {
        this.liter += liter;
        save();
    }

    public void removeLiter(int liter) {
        this.liter -= liter;
        save();
    }
}
