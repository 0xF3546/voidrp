package de.polo.core.oil.listener;

import de.polo.api.VoidAPI;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.oil.services.OilService;
import de.polo.core.utils.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Event
public class OilPumpTickListener implements Listener {
    @EventHandler
    public void onOilPumpTick(MinuteTickEvent event) {
        if (event.getMinute() % 5 != 0) return;
        OilService oilService = VoidAPI.getService(OilService.class);
        oilService.getOilPumps().forEach(oilPump -> {
           oilPump.setOil(oilPump.getOil() + oilPump.getOilPerTick());
           if (oilPump.getOil() > oilPump.getMaxOil()) {
               oilPump.setOil(oilPump.getMaxOil());
           }
              oilService.updateOilPump(oilPump);
        });
    }
}
