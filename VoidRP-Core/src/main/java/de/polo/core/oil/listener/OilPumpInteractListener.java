package de.polo.core.oil.listener;

import de.polo.api.VoidAPI;
import de.polo.api.oil.OilPump;
import de.polo.core.oil.gui.OilPumpGUI;
import de.polo.core.oil.services.OilService;
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
public class OilPumpInteractListener implements Listener {
    @EventHandler
    public void onOilPumpInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.IRON_DOOR) return;
        OilService oilService = VoidAPI.getService(OilService.class);
        OilPump oilPump = oilService.getNearestOilPump(event.getPlayer().getLocation(), 10);
        if (oilPump == null) return;
        new OilPumpGUI(VoidAPI.getPlayer(event.getPlayer()), oilPump).open();
    }
}
