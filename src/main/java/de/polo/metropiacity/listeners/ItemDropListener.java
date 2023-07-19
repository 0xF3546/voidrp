package de.polo.metropiacity.listeners;

import de.polo.metropiacity.dataStorage.WeaponData;
import de.polo.metropiacity.utils.Weapons;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements Listener {
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        WeaponData weaponData = Weapons.weaponDataMap.get(event.getItemDrop().getItemStack().getType());
        if (weaponData != null) {
            event.setCancelled(true);
            Weapons.reloadWeapon( event.getPlayer(), event.getItemDrop().getItemStack());
        }
    }
}
