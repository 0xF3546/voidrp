package de.polo.core.admin.listener;

import de.polo.core.admin.utils.ServerStats;
import de.polo.core.utils.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@Event
public class PeakPlayerCheckListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int currentPlayers = event.getPlayer().getServer().getOnlinePlayers().size();
        if (currentPlayers > ServerStats.getPeakPlayers()) {
            ServerStats.setPeakPlayers(currentPlayers);
        }
    }
}
