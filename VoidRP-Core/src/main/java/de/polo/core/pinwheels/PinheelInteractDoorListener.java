package de.polo.core.pinwheels;

import de.polo.api.VoidAPI;
import de.polo.api.pinwheels.Pinwheel;
import de.polo.core.pinwheels.services.PinwheelService;
import de.polo.core.utils.Event;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Event
public class PinheelInteractDoorListener implements Listener {
    @EventHandler
    public void onPinwheelInteractDoor(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.IRON_DOOR) return;
        PinwheelService pinwheelService = VoidAPI.getService(PinwheelService.class);
        Pinwheel pinwheel = pinwheelService.getNearestPinwheel(event.getPlayer().getLocation(), 10);
        if (pinwheel == null) return;
        new PinwheelGUI(VoidAPI.getPlayer(event.getPlayer()), pinwheel).open();
    }
}
