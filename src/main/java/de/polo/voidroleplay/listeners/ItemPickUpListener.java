package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class ItemPickUpListener implements Listener {
    public ItemPickUpListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getItem().getItemStack().getType().equals(Material.SKELETON_SKULL) || event.getItem().getItemStack().getType().equals(Material.PLAYER_HEAD)) {
            event.setCancelled(true);
        }
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (ItemManager.getCustomItemCount(player, RoleplayItem.SWAT_SHIELD) >= 1 && event.getItem().getItemStack().getType().equals(RoleplayItem.SWAT_SHIELD.getMaterial())) {
                event.setCancelled(true);
            }
        }
    }
}
