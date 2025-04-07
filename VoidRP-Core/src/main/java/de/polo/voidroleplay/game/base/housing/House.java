package de.polo.voidroleplay.game.base.housing;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.base.crypto.Miner;
import de.polo.voidroleplay.housing.enums.HouseType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class House {
    @Getter
    private final int maxServer;
    @Getter
    private final int maxMiner;
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
    @Setter
    private int miner;
    @Getter
    @Setter
    private HouseType houseType = HouseType.BASIC;
    @Getter
    @Setter
    private List<Miner> activeMiner = new ObjectArrayList<>();
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
        Connection connection = Main.getInstance().coreDatabase.getConnection();
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
        Connection connection = Main.getInstance().coreDatabase.getConnection();
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
        Main.getInstance().getCoreDatabase().insertAndGetKeyAsync("INSERT INTO crypto_miner (houseNumber) VALUE (?)",
                number)
                .thenApply(key -> {
                    key.ifPresent(miner::setId);
                    return null;
                });
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
        return mieterSlots + houseType.getBaseSlots();
    }

    public void addMoney(int amount, String reason, boolean silent) {
        setMoney(money + amount);
        if (!silent) sendMessage(reason);
        updateMoney();
    }

    public void removeMoney(int amount, String reason, boolean silent) {
        setMoney(money - amount);
        if (!silent) sendMessage(reason);
        updateMoney();
    }

    private void updateMoney() {
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE housing SET money = ?, totalmoney = ? WHERE number = ?", money, totalMoney, number);
    }

    public void sendMessage(String message) {
        if (owner == null || message == null) return;
        Player player = Bukkit.getPlayer(UUID.fromString(owner));
        if (player == null) return;
        player.sendMessage("§8[§6Haus " + number + "§8]§7 " + message);
    }
}
