package de.polo.void_roleplay.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class playerMoveListener implements Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        /*PlayerData playerData = PlayerManager.playerDataMap.get(event.getPlayer().getUniqueId().toString());
        if (playerData.canInteract()) {
            event.getPlayer().sendMessage(Main.debug_prefix + "Freezed");
            event.setCancelled(true);
        } else {
            event.getPlayer().sendMessage(Main.debug_prefix + "nicht freezed");
        }*/
    }
}
