package de.polo.core.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.polo.core.Main;
import de.polo.core.utils.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static de.polo.core.Main.playerManager;

@Event
public class JumpListener implements Listener {

    @EventHandler
    public void onJump(PlayerJumpEvent event){
        if(playerManager.getPlayerData(event.getPlayer()).isCuffed()){
        event.getPlayer().teleport(event.getFrom().setDirection(event.getTo().getDirection()));
        }
    }
}
