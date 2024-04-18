package de.polo.voidroleplay.game.base.shops;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.Company;
import de.polo.voidroleplay.game.base.shops.ShopItem;
import de.polo.voidroleplay.utils.enums.ShopType;
import lombok.SneakyThrows;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class ShopData {
    private int id;
    private String name;
    private int x;
    private int y;
    private int z;
    private World welt;
    private float yaw;
    private float pitch;
    private ShopType type;
    private int company;
    private int bank;
    private List<ShopItem> items = new ArrayList<>();

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

    public double getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public ShopType getType() {
        return type;
    }

    public void setType(ShopType type) {
        this.type = type;
    }

    public Company getCompany() {
        return Main.getInstance().companyManager.getCompanyById(company);
    }

    public void setCompany(int company) {
        this.company = company;
    }

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE shops SET company = ? WHERE id = ?");
        statement.setInt(1, getCompany().getId());
        statement.setInt(2, getId());
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

    public List<ShopItem> getItems() {
        return items;
    }

    public void setItems(List<ShopItem> items) {
        this.items = items;
    }
    public void removeItem(ShopItem shopItem) {
        items.remove(shopItem);
    }

    public void addItem(ShopItem shopItem) {
        items.add(shopItem);
    }
}
