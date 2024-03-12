package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;

public class ArmorStandExitListener implements Listener {
    public ArmorStandExitListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onVehicle(EntityDismountEvent event) {
        Player player = (Player) event.getEntity();
        if (event.getDismounted() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getDismounted();
            if (!armorStand.isValid()) {
                return;
            }
            armorStand.remove();
        }
    }
}
