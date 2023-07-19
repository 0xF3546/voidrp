package de.polo.metropiacity.listeners;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener {
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.isDead()) {
            if (playerData.getVariable("current_lobby") == null) {
                event.setRespawnLocation(playerData.getDeathLocation());
                player.sendTitle("§cDu bist gestorben.", null, 1, 12, 1);
                player.setGameMode(GameMode.SPECTATOR);
                player.setFlySpeed(0);
            }
        }
    }
}
