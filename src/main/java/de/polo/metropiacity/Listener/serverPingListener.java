package de.polo.metropiacity.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class serverPingListener implements Listener {
    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        event.setMotd("§6§lVoid Roleplay §8| §ewww.voidroleplay.de \n§8➥ §cRoleplay mit Stil. §8 - §bⓘ§a V1.0 bald online!");
    }
}
