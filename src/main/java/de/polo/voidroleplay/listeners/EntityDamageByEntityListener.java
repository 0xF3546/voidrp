package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.WeaponData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.WeaponManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class EntityDamageByEntityListener implements Listener {
    private final PlayerManager playerManager;

    public EntityDamageByEntityListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            WeaponData currentWeapon = null;
            for (WeaponData weaponData : WeaponManager.weaponDataMap.values()) {
                if (!damager.getInventory().getItemInMainHand().getType().equals(weaponData.getMaterial())) {
                    continue;
                }
                if (weaponData.isMeele()) {
                    currentWeapon = weaponData;
                    continue;
                }
                event.setCancelled(true);
                return;
            }
            if (currentWeapon != null) event.setDamage(currentWeapon.getDamage());
            PlayerData playerData = playerManager.getPlayerData(event.getDamager().getUniqueId());
            if ((playerData.getVisum() <= 2 && playerData.getFaction() == null) || playerData.isCuffed()) {
                event.setCancelled(true);
            }
            if (event.getEntity() instanceof Player) {
                PlayerData ownPlayerData = playerManager.getPlayerData((Player) event.getEntity());
                if (ownPlayerData != null) {
                    if (ownPlayerData.getVisum() <= 2 && playerData.getFaction() == null) {
                        event.setCancelled(true);
                    }
                }
            }
            if ((event.getEntity().getType() == EntityType.ARMOR_STAND
                    || event.getEntity().getType() == EntityType.ITEM_FRAME
                    || event.getEntity().getType() == EntityType.PAINTING
                    || event.getEntity().getType() == EntityType.MINECART)
                    && !playerManager.getPlayerData(event.getDamager().getUniqueId()).isAduty()) {
                event.setCancelled(true);
            }
        }
        if ((event.getEntity().getType() == EntityType.ARMOR_STAND
                || event.getEntity().getType() == EntityType.ITEM_FRAME
                || event.getEntity().getType() == EntityType.PAINTING
                || event.getEntity().getType() == EntityType.MINECART)
                && !playerManager.getPlayerData(event.getDamager().getUniqueId()).isAduty()) {
            event.setCancelled(true);
        }
        if (event.getEntity().getType() == EntityType.VILLAGER) {
            String command = event.getEntity().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "command"), PersistentDataType.STRING);
            if (command == null) return;
            event.setCancelled(true);
        }
    }
}
