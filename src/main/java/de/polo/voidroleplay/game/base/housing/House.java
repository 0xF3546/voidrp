package de.polo.voidroleplay.game.base.housing;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.base.crypto.Miner;
import de.polo.voidroleplay.utils.enums.HouseType;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class House {
    @Setter
    private int id;
    @Setter
    private String owner;
    @Setter
    private int number;
    @Setter
    private int price;
    @Setter
    private HashMap<String, Integer> renter = new HashMap<>();
    @Setter
    private int money;
    @Setter
    private int totalMoney;
    @Setter
    private int mieterSlots;
    @Setter
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
    @Setter
    private HouseType houseType = HouseType.BASIC;

    @Getter
    @Setter
    private List<Miner> activeMiner = new ArrayList<>();

    @Getter
    private final int maxMiner;

    @Getter
    @Setter
    private boolean cookActive;

    public House(int number, int maxServer, int maxMiner) {
        this.maxServer = maxServer;
        this.maxMiner = maxMiner;
        this.number = number;
        loadMiner();
    }

    @SneakyThrows
    private void loadMiner() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM crypto_miner WHERE houseNumber = ?");
        statement.setInt(1, number);
        ResultSet res = statement.executeQuery();
        while (res.next()) {
            Miner m = new Miner();
            m.setActive(res.getBoolean("active"));
            m.setId(res.getInt("id"));
            m.setKWh(res.getFloat("kWh"));
            m.setCoins(res.getFloat("coins"));
            activeMiner.add(m);
        }

    }

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE housing SET miner = ?, server = ?, hasServerRoom = ? WHERE id = ?");
        statement.setInt(1, miner);
        statement.setInt(2, server);
        statement.setBoolean(3, serverRoom);
        statement.setInt(4, id);
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    @SneakyThrows
    public void addMiner(Miner miner) {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO crypto_miner (houseNumber) VALUE (?)", Statement.RETURN_GENERATED_KEYS);
        statement.setInt(1, number);
        statement.execute();
        ResultSet result = statement.getGeneratedKeys();
        if (result.next()) {
            miner.setId(result.getInt(1));
        }
        activeMiner.add(miner);
        setMiner(getMiner() + 1);
        save();
    }

    public int getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public int getNumber() {
        return number;
    }

    public int getPrice() {
        return price;
    }

    public HashMap<String, Integer> getRenter() {
        return renter;
    }

    public void addRenter(String uuid, int miete) {
        this.renter.put(uuid, miete);
    }

    public int getMoney() {
        return money;
    }

    public int getTotalMoney() {
        return totalMoney;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public int getMieterSlots() {
        return mieterSlots;
    }

}
