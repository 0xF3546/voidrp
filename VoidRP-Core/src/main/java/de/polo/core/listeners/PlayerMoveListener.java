package de.polo.core.listeners;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Event;
import de.polo.core.utils.player.PlayerPacket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import static de.polo.core.Main.playerManager;
import static de.polo.core.Main.utils;

@Event
public class PlayerMoveListener implements Listener {

    public PlayerMoveListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setIntVariable("afk", 0);
        if (playerData.isAFK()) {
            utils.setAFK(player, false);
            PlayerPacket packet = new PlayerPacket(player);
            packet.renewPacket();
        }
    }
}
