package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.WeaponData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.WeaponManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
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
        if (event.getDamager() instanceof Player || event.getDamager() instanceof Projectile) {
            Player damager = null;

            if (event.getDamager() instanceof Player) {
                damager = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                if (projectile.getShooter() instanceof Player) {
                    damager = (Player) projectile.getShooter();
                }
            }

            if (damager != null) {
                PlayerData damagerData = playerManager.getPlayerData(damager.getUniqueId());

                if (isInSupportOrJail(damager)) {
                    event.setCancelled(true);
                    return;
                }

                handleWeaponDamage(event, damager);

                if (isProtectedEntity(event.getEntity(), damagerData)) {
                    event.setCancelled(true);
                }
            }
        }

        if (event.getEntity().getType() == EntityType.VILLAGER) {
            String command = event.getEntity().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "command"), PersistentDataType.STRING);
            if (command != null) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isInSupportOrJail(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        return playerData.isJailed() || Main.supportManager.getTicket(player) != null;
    }

    private void handleWeaponDamage(EntityDamageByEntityEvent event, Player damager) {
        WeaponData currentWeapon = null;

        for (WeaponData weaponData : WeaponManager.weaponDataMap.values()) {
            if (damager.getInventory().getItemInMainHand().getType().equals(weaponData.getMaterial())) {
                if (weaponData.isMeele()) {
                    currentWeapon = weaponData;
                } else {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (currentWeapon != null) {
            event.setDamage(currentWeapon.getDamage());
        }

        PlayerData playerData = playerManager.getPlayerData(damager.getUniqueId());
        if ((playerData.getVisum() < 2 && playerData.getFaction() == null) || playerData.isCuffed()) {
            event.setCancelled(true);
        }
    }

    private boolean isProtectedEntity(Entity entity, PlayerData playerData) {
        return (entity.getType() == EntityType.ARMOR_STAND
                || entity.getType() == EntityType.ITEM_FRAME
                || entity.getType() == EntityType.PAINTING
                || entity.getType() == EntityType.MINECART
                || entity.getType() == EntityType.BOAT)
                && !playerData.isAduty();
    }
}
