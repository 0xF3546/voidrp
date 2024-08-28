package de.polo.voidroleplay.game.base.housing;

import de.polo.voidroleplay.Main;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;

public class House {
    private int id;
    private String owner;
    private int number;
    private int price;
    private HashMap<String, Integer> renter = new HashMap<>();
    private int money;
    private int totalMoney;
    private int mieterSlots;
    private int totalSlots;

    @Getter
    @Setter
    private boolean serverRoom;

    @Getter
    @Setter
    private int server;

    @Getter
    private final int maxServer;

    @Getter
    @Setter
    private int miner;

    @Getter
    private final int maxMiner;

    public House(int maxServer, int maxMiner) {
        this.maxServer = maxServer;
        this.maxMiner = maxMiner;
    }

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE housing SET miner = ?, server = ?, hasServerRoom = ? WHERE id = ?");
        statement.setInt(1, miner);
        statement.setInt(server, 2);
        statement.setBoolean(3, serverRoom);
        statement.setInt(4, id);
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public HashMap<String, Integer> getRenter() {
        return renter;
    }

    public void setRenter(HashMap<String, Integer> map) {
        this.renter = map;
    }

    public void addRenter(String uuid, int miete) {
        this.renter.put(uuid, miete);
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(int totalMoney) {
        this.totalMoney = totalMoney;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public int getMieterSlots() {
        return mieterSlots;
    }

    public void setMieterSlots(int mieterSlots) {
        this.mieterSlots = mieterSlots;
    }
}
