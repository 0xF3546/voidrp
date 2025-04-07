package de.polo.voidroleplay.housing.services.impl;

import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.housing.services.HouseService;
import de.polo.voidroleplay.storage.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

import static de.polo.voidroleplay.Main.houseManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
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
    public void addHouseSlot(Player player) throws Exception {
        houseManager.addHausSlot(player);
    }

    @Override
    public boolean resetHouse(Player player, int house) throws Exception {
        return houseManager.resetHouse(player, house);
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
}
