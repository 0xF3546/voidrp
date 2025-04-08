package de.polo.core.storage;

import de.polo.core.Main;
import lombok.SneakyThrows;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class GasStationData {
    private int id;
    private String name;
    private int x;
    private int y;
    private int z;
    private World welt;
    private float yaw;
    private float pitch;
    private int price;
    private int literprice;
    private int liter;
    private int company;
    private int bank;

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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public World getWelt() {
        return welt;
    }

    public void setWelt(World welt) {
        this.welt = welt;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getLiterprice() {
        return literprice;
    }

    public void setLiterprice(int literprice) {
        this.literprice = literprice;
    }

    public int getLiter() {
        return liter;
    }

    public void setLiter(int liter) {
        this.liter = liter;
    }

    public Company getCompany() {
        return Main.getInstance().companyManager.getCompanyById(company);
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

    public int getBank() {
        return bank;
    }

    public void setBank(int bank) {
        this.bank = bank;
    }
}
