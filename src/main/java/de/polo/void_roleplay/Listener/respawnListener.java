package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class respawnListener implements Listener {
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.isDead()) {
            System.out.println("spieler ist tot");
            System.out.println(playerData.getVariable("current_lobby"));
            if (playerData.getVariable("current_lobby") == null) {
                event.setRespawnLocation(playerData.getDeathLocation());
                player.sendTitle("§cDu bist gestorben.", null, 1, 6, 1);
            }
        }
    }
}
