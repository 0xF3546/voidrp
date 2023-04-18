package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import sun.net.www.protocol.mailto.MailToURLConnection;

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
