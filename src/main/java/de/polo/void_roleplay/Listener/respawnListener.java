package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Location;
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
            //player.teleport(loc);
            player.sleep(player.getLocation(), true);
            player.sendMessage(Main.debug_prefix + "respawned");
        }
    }
}
