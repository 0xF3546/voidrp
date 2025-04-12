package de.polo.core.zone;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.api.zone.EnterZoneEvent;
import de.polo.api.zone.ExitZoneEvent;
import de.polo.api.zone.Zone;
import de.polo.core.Main;
import de.polo.core.utils.Event;
import de.polo.core.zone.services.ZoneService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Event
public class ZoneEvents implements Listener {

    private final Map<VoidPlayer, Zone> playerZoneMap = new WeakHashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        handleZoneCheck(event.getPlayer().getLocation(), VoidAPI.getPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            handleZoneCheck(event.getPlayer().getLocation(), VoidAPI.getPlayer(event.getPlayer()));
        }, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        VoidPlayer player = VoidAPI.getPlayer(event.getPlayer());
        Zone currentZone = playerZoneMap.remove(player);

        if (currentZone != null) {
            currentZone.removePlayer(player);
            Bukkit.getPluginManager().callEvent(new ExitZoneEvent(player, currentZone));
        }
    }

    private void handleZoneCheck(Location location, VoidPlayer player) {
        Zone currentZone = playerZoneMap.get(player);
        Zone newZone = null;
        ZoneService zoneService = VoidAPI.getService(ZoneService.class);

        List<Zone> zones = zoneService.getZones();
        for (Zone zone : zones) {
            if (zone.getLocation().getWorld().equals(location.getWorld())
                    && zone.getLocation().distance(location) <= zone.getRange()) {
                newZone = zone;
                break;
            }
        }

        if (currentZone != null && !currentZone.equals(newZone)) {
            currentZone.removePlayer(player);
            playerZoneMap.remove(player);
            Bukkit.getPluginManager().callEvent(new ExitZoneEvent(player, currentZone));
        }

        if (newZone != null && !newZone.equals(currentZone)) {
            newZone.addPlayer(player);
            playerZoneMap.put(player, newZone);
            Bukkit.getPluginManager().callEvent(new EnterZoneEvent(player, newZone));
        }
    }
}
