package de.polo.core.listeners;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Event;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Event
public class PlayerSpawnpointListener implements Listener {

    @EventHandler
    public void onPlayerRespawn(PlayerSetSpawnEvent event) {
        event.getPlayer().sendMessage(Component.text(
                        Prefix.ERROR + "Du bist nicht berechtigt, deinen Respawn-Punkt zu ändern.").
                color(NamedTextColor.RED));
        event.setCancelled(true);
    }
}
