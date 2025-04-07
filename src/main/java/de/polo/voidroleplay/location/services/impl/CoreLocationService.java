package de.polo.voidroleplay.location.services.impl;

import de.polo.voidroleplay.location.services.LocationService;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.GasStationData;
import de.polo.voidroleplay.storage.LocationData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

import static de.polo.voidroleplay.Main.locationManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreLocationService implements LocationService {

    @Override
    public void setLocation(String name, Player player) {
        locationManager.setLocation(name, player);
    }

    @Override
    public void useLocation(Player player, String name) {
        locationManager.useLocation(player, name);
    }

    @Override
    public Location getLocation(String name) {
        return locationManager.getLocation(name);
    }

    @Override
    public double getDistanceBetweenCoords(Player player, String name) {
        return locationManager.getDistanceBetweenCoords(player, name);
    }

    @Override
    public double getDistanceBetweenCoords(VoidPlayer player, String name) {
        return locationManager.getDistanceBetweenCoords(player, name);
    }

    @Override
    public int isNearShop(Player player) {
        return locationManager.isNearShop(player);
    }

    @Override
    public String getShopNameById(Integer id) {
        return locationManager.getShopNameById(id);
    }

    @Override
    public Integer isPlayerNearOwnHouse(Player player) {
        return locationManager.isPlayerNearOwnHouse(player);
    }

    @Override
    public Integer isPlayerGasStation(Player player) {
        return locationManager.isPlayerGasStation(player);
    }

    @Override
    public GasStationData getGasStationInRadius(Player player) {
        return locationManager.getGasStationInRadius(player);
    }

    @Override
    public String getNearestLocation(Player player) {
        return locationManager.getNearestLocation(player);
    }

    @Override
    public Integer getNearestLocationId(Player player) {
        return locationManager.getNearestLocationId(player);
    }

    @Override
    public String isNearFarmingSpot(Player player, int radius) {
        return locationManager.isNearFarmingSpot(player, radius);
    }

    @Override
    public Collection<LocationData> getLocations() {
        return locationManager.getLocations();
    }

    @Override
    public boolean isLocationEqual(Location first, Location second) {
        return LocationManager.isLocationEqual(first, second);
    }
}
