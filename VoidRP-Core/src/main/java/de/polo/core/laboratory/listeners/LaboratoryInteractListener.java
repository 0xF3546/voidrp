package de.polo.core.laboratory.listeners;

import de.polo.api.VoidAPI;
import de.polo.api.laboratory.Laboratory;
import de.polo.core.laboratory.gui.LaboratoryGUI;
import de.polo.core.laboratory.services.LaboratoryService;
import de.polo.core.listeners.PlayerInteractListener;
import de.polo.core.utils.Event;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

@Event
public class LaboratoryInteractListener implements Listener {
    @EventHandler
    public void onInteractWithLaboratory(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock() .getType() != Material.BREWING_STAND) return;
        LaboratoryService laboratoryService = VoidAPI.getService(LaboratoryService.class);
        Player player = event.getPlayer();
        Laboratory lab = laboratoryService.getNearestLaboratory(player.getLocation(), 10);
        if (lab == null) return;
        new LaboratoryGUI(VoidAPI.getPlayer(player), lab);
    }
}
