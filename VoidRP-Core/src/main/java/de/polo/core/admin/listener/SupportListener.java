package de.polo.core.admin.listener;

import de.polo.api.VoidAPI;
import de.polo.core.admin.services.AdminService;
import de.polo.core.admin.services.SupportService;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.utils.Event;
import org.bukkit.Color;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Event
public class SupportListener implements Listener {
    @EventHandler
    public void onMinute(MinuteTickEvent event) {
        if (event.getMinute() % 3 != 0) return;
        SupportService supportService = VoidAPI.getService(SupportService.class);
        int ticketsOpen = supportService.getTickets().size();
        if (ticketsOpen == 0) return;
        AdminService adminService = VoidAPI.getService(AdminService.class);
        adminService.sendAdminMessage("Es gibt " + ticketsOpen + " Tickets die noch nicht bearbeitet wurden.", Color.RED);
    }
}
