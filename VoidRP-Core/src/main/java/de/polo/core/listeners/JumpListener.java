package de.polo.core.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.polo.core.Main;
import de.polo.core.player.services.impl.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JumpListener implements Listener {

    private final PlayerManager playerManager;

    public JumpListener(PlayerManager playerManager){
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onJump(PlayerJumpEvent event){
        if(playerManager.getPlayerData(event.getPlayer()).isCuffed()){
        event.getPlayer().teleport(event.getFrom().setDirection(event.getTo().getDirection()));
        }
    }
}
