package de.polo.voidroleplay.game.base.housing;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.voidroleplay.dataStorage.NaviData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.RegisteredBlock;
import de.polo.voidroleplay.utils.BlockManager;
import de.polo.voidroleplay.utils.LocationManager;
import de.polo.voidroleplay.utils.PlayerManager;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Housing {
    public static final Map<Integer, House> houseDataMap = new HashMap<>();
    private final PlayerManager playerManager;
    private final BlockManager blockManager;

    public Housing(PlayerManager playerManager, BlockManager blockManager) {
        this.playerManager = playerManager;
        this.blockManager = blockManager;
        try {
            loadHousing();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadHousing() throws SQLException {
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet locs = statement.executeQuery("SELECT * FROM housing");
        while (locs.next()) {
            House houseData = new House();
            houseData.setId(locs.getInt(1));
            houseData.setOwner(locs.getString(2));
            houseData.setNumber(locs.getInt(3));
            houseData.setPrice(locs.getInt(4));
            houseData.setTotalMoney(locs.getInt(7));

            JSONObject object = new JSONObject(locs.getString(5));
            HashMap<String, Integer> map = new HashMap<>();
            for (String key : object.keySet()) {
                int value = (int) object.get(key);
                map.put(key, value);
            }
            houseData.setRenter(map);
            houseData.setMoney(locs.getInt(6));
            houseDataMap.put(locs.getInt(3), houseData);
        }
    }

    public House getHouse(int houseNumber) {
        return houseDataMap.get(houseNumber);
    }

    public boolean isPlayerOwner(Player player, int number) {
        House houseData = houseDataMap.get(number);
        return player.getUniqueId().toString().equals(houseData.getOwner());
    }

    public boolean canPlayerInteract(Player player, int number) {
        House houseData = houseDataMap.get(number);
        if (!Objects.equals(houseData.getOwner(), player.getUniqueId().toString())) {
            System.out.println("spieler ist kein owner");
            System.out.println(houseData.getRenter().get(player.getUniqueId().toString()));
            return houseData.getRenter().get(player.getUniqueId().toString()) != null;
        } else {
            return true;
        }
    }

    public String getHouseAccessAsString(PlayerData playerData) {
        if (playerData == null || playerData.getUuid() == null) {
            return "";
        }

        StringBuilder returnVal = null;
        for (House houseData : houseDataMap.values()) {
            if (houseData != null && houseData.getOwner() != null) {
                String playerUuid = playerData.getUuid().toString();
                if (houseData.getOwner().equalsIgnoreCase(playerUuid) || (houseData.getRenter() != null && houseData.getRenter().get(playerUuid) != null)) {
                    if (returnVal == null) {
                        returnVal = new StringBuilder(String.valueOf(houseData.getNumber()));
                    } else {
                        returnVal.append(", ").append(houseData.getNumber());
                    }
                }
            }
        }
        if (returnVal == null) {
            return "";
        }
        return returnVal.toString();
    }

    public void updateRenter(int number) {
        House houseData = houseDataMap.get(number);
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            JSONObject object = new JSONObject(houseData.getRenter());
            statement.executeUpdate("UPDATE `housing` SET `renter` = '" + object + "' WHERE `number` = " + number);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<House> getAccessedHousing(Player player) {
        List<House> access = new ArrayList<>();
        for (House houseData : houseDataMap.values()) {
            if (!Objects.equals(houseData.getOwner(), player.getUniqueId().toString())) {
                if (houseData.getRenter().get(player.getUniqueId().toString()) != null) {
                    access.add(houseData);
                }
            } else {
                access.add(houseData);
            }
        }
        return access;
    }

    @SneakyThrows
    public void addHausSlot(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        System.out.println("hausslot hinzugefügt");
        playerData.setHouseSlot(playerData.getHouseSlot() + 1);
        Statement statement = Main.getInstance().mySQL.getStatement();
        statement.executeUpdate("UPDATE `players` SET `houseSlot` = " + playerData.getHouseSlot() + " WHERE `uuid` = '" + player.getUniqueId() + "'");
    }

    public boolean resetHouse(Player player, int house) {
        for (RegisteredBlock rBlock : blockManager.getBlocks()) {
            if (rBlock.getInfo() == null) continue;
            if (rBlock.getInfoValue() == null) continue;

            if (!rBlock.getInfo().equalsIgnoreCase("house")) continue;
            if (!rBlock.getInfoValue().equalsIgnoreCase(String.valueOf(house))) continue;

            System.out.println("Haus: " + house);
            System.out.println(rBlock.getInfo() + " - " + rBlock.getInfoValue());

            Block block = rBlock.getLocation().getBlock();
            System.out.println(block.getType().toString());
            if (block.getType().toString().contains("SIGN")) {
                Sign sign = (Sign) block.getState();
                try {
                    House houseData = Housing.houseDataMap.get(house);
                    System.out.println("HOUSE: " + houseData.getOwner());
                    houseData.setOwner(null);
                    sign.setLine(2, "§aZu Verkaufen");
                    sign.update();
                    Statement statement = Main.getInstance().mySQL.getStatement();
                    statement.executeUpdate("UPDATE `housing` SET `owner` = null WHERE `number` = " + house);
                    return true;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return false;
    }
}
