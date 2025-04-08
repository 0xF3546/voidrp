package de.polo.core.game.base.extra;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.InventoryUtils;
import de.polo.core.utils.enums.Powerup;
import de.polo.core.utils.enums.StorageType;
import de.polo.core.utils.enums.Storages;
import lombok.Getter;
import lombok.Setter;
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
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Storage implements Listener {
    private int id;
    private String player;
    private int factionId = -1;
    private int vehicleId = -1;

    @Getter
    @Setter
    private int houseNumber = -1;

    private StorageType storageType;
    private Inventory inventory;
    private Storages extra;
    private boolean canOpen = true;
    @Getter
    @Setter
    private int size = 27;

    public Storage(StorageType storageType) {
        this.storageType = storageType;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @SneakyThrows
    public static Storage load(int storageId) {
        try (Connection connection = Main.getInstance().coreDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM storages WHERE id = ?")) {

            statement.setInt(1, storageId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Storage storage = new Storage(StorageType.valueOf(resultSet.getString("storageType")));
                    storage.setId(resultSet.getInt("id"));
                    storage.setFactionId(resultSet.getInt("factionId"));
                    storage.setVehicleId(resultSet.getInt("vehicleId"));
                    storage.setPlayer(resultSet.getString("player"));

                    String extra = resultSet.getString("extra");
                    if (extra != null && !extra.isEmpty()) {
                        storage.setExtra(Storages.valueOf(extra));
                    }

                    String inventorySerialized = resultSet.getString("inventory");
                    if (inventorySerialized != null) {
                        storage.setInventory(InventoryUtils.deserializeInventory(inventorySerialized));
                    }

                    return storage;
                }
            }
        }
        return null;
    }


    @SneakyThrows
    public static Storage getStorageByTypeAndPlayer(StorageType storageType, Player player, Object value) {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
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
            case CRYSTAL_LABORATORY:
            case HOUSE:
                statement = connection.prepareStatement("SELECT * FROM storages WHERE houseNumber = ?");
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
            storage.setHouseNumber(resultSet.getInt("houseNumber"));
            storage.setSize(resultSet.getInt("size"));
            if (resultSet.getString("extra") != null) {
                storage.setExtra(Storages.valueOf(resultSet.getString("extra")));
            }
            storage.setInventory(InventoryUtils.deserializeInventory(resultSet.getString("inventory")));
            return storage;
        }
        return null;
    }

    @SneakyThrows
    public static Storage getStorageById(int id) {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM storages WHERE id = ?");
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            Storage storage = new Storage(StorageType.valueOf(resultSet.getString("storageType")));
            storage.setId(resultSet.getInt("id"));
            storage.setFactionId(resultSet.getInt("factionId"));
            storage.setVehicleId(resultSet.getInt("vehicleId"));
            storage.setPlayer(resultSet.getString("player"));
            storage.setHouseNumber(resultSet.getInt("houseNumber"));
            storage.setSize(resultSet.getInt("size"));
            if (resultSet.getString("extra") != null) {
                storage.setExtra(Storages.valueOf(resultSet.getString("extra")));
            }
            storage.setInventory(InventoryUtils.deserializeInventory(resultSet.getString("inventory")));
            return storage;
        }
        return null;
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
            case CRYSTAL_LABORATORY:
                name = "§bKristall-Labor";
                break;
            case HOUSE:
                name = "§7Hauslager";
                break;
        }
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(p);
        if (playerData.getPlayerPowerUpManager().getPowerUp(Powerup.STORAGE).getAmount() > inventory.getSize()) {
            setSize(playerData.getPlayerPowerUpManager().getPowerUp(Powerup.STORAGE).getAmount());
        }
        Inventory newInv = Bukkit.createInventory(p, size, name);
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
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO storages (storageType, factionId, vehicleId, player, extra, inventory, houseNumber) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, storageType.name());
        statement.setInt(2, factionId);
        statement.setInt(3, vehicleId);
        statement.setString(4, player);
        if (extra != null) statement.setString(5, extra.name());
        else statement.setString(5, null);
        if (player != null) {
            this.inventory = Bukkit.createInventory(Bukkit.getPlayer(UUID.fromString(player)), size, "§7Lager");
        } else {
            this.inventory = Bukkit.createInventory(null, size, "§7Lager");
        }
        statement.setString(6, InventoryUtils.serializeInventory(inventory));
        statement.setInt(7, houseNumber);
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
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE storages SET storageType = ?, factionId = ?, vehicleId = ?, player = ?, inventory = ?, extra = ?, size = ?, houseNumber = ? WHERE id = ?");
            statement.setString(1, storageType.name());
            statement.setInt(2, factionId);
            statement.setInt(3, vehicleId);
            statement.setString(4, player);
            statement.setString(5, InventoryUtils.serializeInventory(inventory));
            if (extra != null) statement.setString(6, extra.name());
            else statement.setString(6, null);
            statement.setInt(7, size);
            statement.setInt(8, houseNumber);
            statement.setInt(9, id);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Storages getExtra() {
        return extra;
    }

    public void setExtra(Storages extra) {
        this.extra = extra;
    }

    public boolean isCanOpen() {
        return canOpen;
    }

    public void setCanOpen(boolean canOpen) {
        this.canOpen = canOpen;
    }
}
