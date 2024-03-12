package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class PlayerPickUpArrowListener implements Listener {
    public PlayerPickUpArrowListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onPlayerPickUpArrow(PlayerPickupArrowEvent event) {
        event.setCancelled(true);
        event.getArrow().remove();
    }
}
