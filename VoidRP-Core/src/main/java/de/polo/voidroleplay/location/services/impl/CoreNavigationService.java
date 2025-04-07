package de.polo.voidroleplay.location.services.impl;

import de.polo.voidroleplay.location.services.NavigationService;
import de.polo.voidroleplay.storage.NaviData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static de.polo.voidroleplay.Main.playerManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreNavigationService implements NavigationService {
    private final NavigationManager navigationManager;

    public CoreNavigationService() {
        this.navigationManager = new NavigationManager(playerManager);
    }

    @Override
    public NaviData getNearestNaviPoint(Location location) {
        return NavigationManager.getNearestNaviPoint(location);
    }

    @Override
    public void openNavi(Player player, String search) {
        navigationManager.openNavi(player, search);
    }

    @Override
    public void createNaviByCord(Player player, int x, int y, int z) {
        navigationManager.createNaviByCord(player, x, y, z);
    }

    @Override
    public void createNavi(Player player, String nav, boolean silent) {
        navigationManager.createNavi(player, nav, silent);
    }

    @Override
    public void createNaviByLocation(Player player, String nav) {
        navigationManager.createNaviByLocation(player, nav);
    }
}
