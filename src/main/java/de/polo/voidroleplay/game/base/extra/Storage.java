package de.polo.voidroleplay.game.base.extra;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.InventoryUtils;
import de.polo.voidroleplay.utils.enums.StorageType;
import de.polo.voidroleplay.utils.enums.Storages;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Storage implements Listener {
    private static List<Storage> storages = new ArrayList<>();
    private int id;
    private String player;
    private int factionId = -1;
    private int vehicleId = -1;
    private StorageType storageType;
    private Inventory inventory;
    private Storages extra;
    private boolean canOpen;

    public Storage(StorageType storageType) {
        this.storageType = storageType;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFactionId() {
        return factionId;
    }

    public void setFactionId(int factionId) {
        this.factionId = factionId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public void open(Player p) {
        if (!canOpen) return;
        String name = "§7Lager";
        switch (storageType) {
            case VEHICLE:
                name = "§7Kofferraum";
                break;
            case FACTION:
                name = "§7Fraktionslager";
                break;
            case EXTRA:
                name = "§7Lager " + extra.getName();
                break;
            case HOUSE:
                name = "§7Hauslager";
                break;
        }
        Inventory newInv = Bukkit.createInventory(p, inventory.getSize(), name);
        newInv.setContents(inventory.getContents());
        this.inventory = newInv;
        p.openInventory(inventory);
        setCanOpen(false);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() == inventory) {
            setCanOpen(true);
            save();
        }
    }

    @SneakyThrows
    public void create() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO storages (storageType, factionId, vehicleId, player, extra, inventory) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, storageType.name());
        statement.setInt(2, factionId);
        statement.setInt(3, vehicleId);
        statement.setString(4, player);
        if (extra != null) statement.setString(5, extra.name());
        else statement.setString(5, null);
        if (player != null) {
            this.inventory = Bukkit.createInventory(Bukkit.getPlayer(UUID.fromString(player)), 27, "§7Lager");
        } else {
            this.inventory = Bukkit.createInventory(null, 27, "§7Lager");
        }
        statement.setString(6, InventoryUtils.serializeInventory(inventory));
        statement.execute();
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            int key = generatedKeys.getInt(1);
            setId(key);
        }
    }
    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE storages SET storageType = ?, factionId = ?, vehicleId = ?, player = ?, inventory = ?, extra = ? WHERE id = ?");
            statement.setString(1, storageType.name());
            statement.setInt(2, factionId);
            statement.setInt(3, vehicleId);
            statement.setString(4, player);
            statement.setString(5, InventoryUtils.serializeInventory(inventory));
            if (extra != null) statement.setString(6, extra.name());
            else statement.setString(6, null);
            statement.setInt(7, id);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public static Storage load(int storageId) {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM storages WHERE id = ?");
        statement.setInt(1, storageId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            Storage storage = new Storage(StorageType.valueOf(resultSet.getString("storageType")));
            storage.setId(resultSet.getInt("id"));
            storage.setFactionId(resultSet.getInt("factionId"));
            storage.setVehicleId(resultSet.getInt("vehicleId"));
            storage.setPlayer(resultSet.getString("player"));
            if (resultSet.getString("extra") != null) {
                storage.setExtra(Storages.valueOf(resultSet.getString("extra")));
            }
            storage.setInventory(InventoryUtils.deserializeInventory(resultSet.getString("inventory")));
            return storage;
        }
        return null;
    }

    public Storages getExtra() {
        return extra;
    }

    public void setExtra(Storages extra) {
        this.extra = extra;
    }

    @SneakyThrows
    public static Storage getStorageByTypeAndPlayer(StorageType storageType, Player player, Object value) {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM storages WHERE player = ?");
        statement.setString(1, player.getUniqueId().toString());
        switch (storageType) {
            case EXTRA:
                Storages type = (Storages) value;
                statement = connection.prepareStatement("SELECT * FROM storages WHERE player = ? AND extra = ?");
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, type.name());
                break;
            case VEHICLE:
                statement = connection.prepareStatement("SELECT * FROM storages WHERE vehicleId = ?");
                statement.setInt(1, (int) value);
                break;
            case FACTION:
                statement = connection.prepareStatement("SELECT * FROM storages WHERE factionId = ?");
                statement.setInt(1, (int) value);
                break;
        }
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            Storage storage = new Storage(StorageType.valueOf(resultSet.getString("storageType")));
            storage.setId(resultSet.getInt("id"));
            storage.setFactionId(resultSet.getInt("factionId"));
            storage.setVehicleId(resultSet.getInt("vehicleId"));
            storage.setPlayer(resultSet.getString("player"));
            if (resultSet.getString("extra") != null) {
                storage.setExtra(Storages.valueOf(resultSet.getString("extra")));
            }
            storage.setInventory(InventoryUtils.deserializeInventory(resultSet.getString("inventory")));
            return storage;
        }
        return null;
    }

    public boolean isCanOpen() {
        return canOpen;
    }

    public void setCanOpen(boolean canOpen) {
        this.canOpen = canOpen;
    }
}
