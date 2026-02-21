package de.polo.core.housing.services.impl;

import de.polo.api.utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.game.base.housing.House;
import de.polo.core.housing.enums.HouseType;
import de.polo.core.housing.services.HouseService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.utils.Service;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static de.polo.core.Main.*;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class CoreHouseService implements HouseService {

    @Override
    public House getHouse(int houseNumber) {
        return houseManager.getHouse(houseNumber);
    }

    @Override
    public void addHouse(House house) {
        houseManager.addHouse(house);
    }

    @Override
    public boolean isPlayerOwner(Player player, int number) {
        return houseManager.isPlayerOwner(player, number);
    }

    @Override
    public boolean canPlayerInteract(Player player, int number) {
        return houseManager.canPlayerInteract(player, number);
    }

    @Override
    public String getHouseAccessAsString(PlayerData playerData) {
        return houseManager.getHouseAccessAsString(playerData);
    }

    @Override
    public void updateRenter(int number) throws Exception {
        houseManager.updateRenter(number);
    }

    @Override
    public List<House> getAccessedHousing(Player player) {
        return houseManager.getAccessedHousing(player);
    }

    @Override
    public House getNearestHouse(Location loc, int range) {
        return houseManager.getNearestHouse(loc, range);
    }

    @Override
    public Collection<House> getHouses(Player player) {
        return houseManager.getHouses(player);
    }

    @Override
    public Collection<House> getHouses() {
        return houseManager.getHouses();
    }

    @Override
    public void addHouseSlot(Player player) throws Exception {
        houseManager.addHausSlot(player);
    }

    @Override
    public boolean resetHouse(int house) throws Exception {
        return houseManager.resetHouse(house);
    }

    @Override
    public void openHouseServerRoom(Player player, House house) {
        houseManager.openHouseServerRoom(player, house);
    }

    @Override
    public void doCryptoTick() {
        houseManager.doCryptoTick();
    }

    @Override
    public void openCookMenu(Player player, House house) {
        houseManager.openCookMenu(player, house);
    }

    @Override
    public void openGunCabinet(Player player, House house) {
        houseManager.openGunCabinet(player, house);
    }

    @Override
    public void openHouseTreasury(Player player, House house) {
        houseManager.openHouseTreasury(player, house);
    }

    @Override
    public void setMieterSlot(int houseNumber, int mieter) {
        houseManager.setMieterSlot(houseNumber, mieter);
    }

    @Override
    public void deleteHouse(int houseNumber) {
        houseManager.getHouses().removeIf(house -> house.getNumber() == houseNumber);
        database.deleteAsync("DELETE FROM houses WHERE number = ?", houseNumber);
    }

    @SneakyThrows
    @Override
    public void refundHouse(int houseNumber) {
        House house = houseManager.getHouse(houseNumber);
        String owner = house.getOwner();
        if (owner != null) {
            VoidPlayer player = VoidAPI.getPlayer(UUID.fromString(owner));
            if (player != null) {
                player.sendMessage("Dein Haus " + houseNumber + " wurde rückerstattet. Du erhälst " + house.getPrice(), Prefix.ADMIN);
                player.getData().addBankMoney(house.getPrice(), "Rückerstattung für Haus " + houseNumber);
            } else {
                database.updateAsync("UPDATE players SET bank = bank + ? WHERE uuid = ?", house.getPrice(), owner);
            }
        }
        resetHouse(houseNumber);
        updateSign(house);
    }

    @Override
    public void setHousePrice(int houseNumber, int price) {
        House house = houseManager.getHouse(houseNumber);
        if (house != null) {
            house.setPrice(price);
            database.updateAsync("UPDATE houses SET price = ? WHERE number = ?", price, houseNumber);
        }
    }

    @Override
    public void updateSign(House house) {
        RegisteredBlock registeredBlock = blockManager.getBlocks().stream()
                .filter(x -> x.getInfo().equalsIgnoreCase("house") && x.getInfoValue().equalsIgnoreCase(String.valueOf(house.getNumber())))
                .findFirst()
                .orElse(null);
        if (registeredBlock == null) return;
        Block block = registeredBlock.getBlock();
        if (block == null) return;
        TileState state = (TileState) block.getState();
        if (!(state instanceof Sign sign)) return;
        if (house.getOwner() == null) {
            sign.setLine(1, "== §6Haus " + house.getNumber() + " §0==");
            sign.setLine(2, "§2Zu Verkaufen");
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(house.getOwner()));
            sign.setLine(1, "== §6Haus " + house.getNumber() + " §0==");
            sign.setLine(2, "§8" + offlinePlayer.getName());
        }
        sign.update();
    }

    @Override
    public void updateType(House house, HouseType houseType) {
        house.setHouseType(houseType);
        database.updateAsync("UPDATE houses SET type = ? WHERE number = ?", houseType.name(), house.getNumber());
    }
}
