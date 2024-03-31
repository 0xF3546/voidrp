package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.WeaponData;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Weapons;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements Listener {
    private final Weapons weapons;
    private final PlayerManager playerManager;
    public ItemDropListener(Weapons weapons, PlayerManager playerManager) {
        this.weapons = weapons;
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        PlayerData playerData = playerManager.getPlayerData(event.getPlayer());
        if (playerData.getVariable("gangwar") != null) event.setCancelled(true);
        WeaponData weaponData = Weapons.weaponDataMap.get(event.getItemDrop().getItemStack().getType());
        if (weaponData != null) {
            event.setCancelled(true);
            weapons.reloadWeapon(event.getPlayer(), event.getItemDrop().getItemStack());
        }
    }
}
