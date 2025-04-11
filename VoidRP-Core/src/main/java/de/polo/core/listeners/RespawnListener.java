package de.polo.core.listeners;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.location.services.LocationService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.gameplay.MilitaryDrop;
import de.polo.core.utils.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import static de.polo.core.Main.playerManager;

@Event
public class RespawnListener implements Listener {

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return;
        float flySpeed = 0;
        if (event.isBedSpawn()) {
            event.setRespawnLocation(playerData.getDeathLocation());
        }
        if (!playerData.isDead()) {
            if (MilitaryDrop.ACTIVE) {
                if (!Main.getInstance().gamePlay.militaryDrop.isPlayerInEvent(player)) {
                    return;
                } else {
                    flySpeed = 0.1F;
                }
            }
        }
        /*if (playerData.getVariable("ffa") != null) {
            Main.getInstance().gamePlay.getFfa().handleDeath(player);
            return;
        }*/
        player.sendTitle("§cDu bist gestorben.", null, 1, 12, 1);
        // player.setGameMode(GameMode.SPECTATOR);
        player.setFlySpeed(flySpeed);
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (playerData.getKarma() < -50) {
            event.setRespawnLocation(locationService.getLocation("hell"));
            locationService.useLocation(player,"hell");
        } else if (playerData.getKarma() >= 50) {
            event.setRespawnLocation(locationService.getLocation("heaven"));
            locationService.useLocation(player,"heaven");
        } else {
            event.setRespawnLocation(locationService.getLocation("cemetery"));
            locationService.useLocation(player, "cemetery");
        }
    }
}
