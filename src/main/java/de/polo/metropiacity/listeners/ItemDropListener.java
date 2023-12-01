package de.polo.metropiacity.listeners;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.WeaponData;
import de.polo.metropiacity.utils.Weapons;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements Listener {
    private final Weapons weapons;
    public ItemDropListener(Weapons weapons) {
        this.weapons = weapons;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        WeaponData weaponData = Weapons.weaponDataMap.get(event.getItemDrop().getItemStack().getType());
        if (weaponData != null) {
            event.setCancelled(true);
            weapons.reloadWeapon(event.getPlayer(), event.getItemDrop().getItemStack());
        }
    }
}
