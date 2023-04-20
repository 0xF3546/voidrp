package de.polo.void_roleplay.Listener;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Objects;

public class playerLoginListener implements Listener {
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_BANNED) {
            event.setKickMessage(Objects.requireNonNull(Bukkit.getBanList(BanList.Type.NAME).getBanEntry(event.getPlayer().getName()).getReason()));
        }
    }
}
