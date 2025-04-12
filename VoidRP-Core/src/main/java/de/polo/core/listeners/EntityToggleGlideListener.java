package de.polo.core.listeners;

import de.polo.core.Main;
import de.polo.core.utils.enums.RoleplayItem;
import de.polo.core.utils.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.ItemStack;

@Event
public class EntityToggleGlideListener implements Listener {

    @EventHandler
    public void onPlayerLand(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (!event.isGliding()) {
            ItemStack chestplate = player.getInventory().getChestplate();
            if (chestplate != null && chestplate.getType() == RoleplayItem.WINGSUIT.getMaterial()) {
                player.getInventory().setChestplate(null);
                player.sendMessage("§8[§bWingsuit§8] §bDeine Elytra wurde entfernt, da du gelandet bist.");
            }
        }
    }
}
