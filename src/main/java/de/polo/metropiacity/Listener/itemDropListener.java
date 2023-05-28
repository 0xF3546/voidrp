package de.polo.metropiacity.Listener;

import de.polo.metropiacity.DataStorage.WeaponData;
import de.polo.metropiacity.Utils.Weapons;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class itemDropListener implements Listener {
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        WeaponData weaponData = Weapons.weaponDataMap.get(event.getItemDrop().getItemStack().getType());
        if (weaponData != null) {
            event.setCancelled(true);
            Weapons.reloadWeapon( event.getPlayer(), event.getItemDrop().getItemStack());
        }
    }
}
