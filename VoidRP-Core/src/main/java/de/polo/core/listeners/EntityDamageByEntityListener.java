package de.polo.core.listeners;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.enums.Weapon;
import de.polo.core.utils.Event;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import static de.polo.core.Main.playerManager;
import static de.polo.core.Main.supportManager;

@Event
public class EntityDamageByEntityListener implements Listener {
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
                VoidPlayer voidDamager = VoidAPI.getPlayer(damager);

                if (isInSupportOrJail(damager)) {
                    event.setCancelled(true);
                    return;
                }

                handleWeaponDamage(event, damager);

                if (isProtectedEntity(event.getEntity(), voidDamager)) {
                    event.setCancelled(true);
                }
            }
        }

        if (event.getEntity().getType() == EntityType.VILLAGER) {
            String command = event.getEntity().getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "command"), PersistentDataType.STRING);
            if (command != null) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isInSupportOrJail(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        return playerData.isJailed() || supportManager.isInAcceptedTicket(player);
    }

    private void handleWeaponDamage(EntityDamageByEntityEvent event, Player damager) {
        Weapon currentWeapon = null;

        for (Weapon weapon : Weapon.values()) {
            if (damager.getInventory().getItemInMainHand().getType().equals(weapon.getMaterial())) {
                if (weapon.isMeele()) {
                    currentWeapon = weapon;
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

    private boolean isProtectedEntity(Entity entity, VoidPlayer playerData) {
        return (entity.getType() == EntityType.ARMOR_STAND
                || entity.getType() == EntityType.ITEM_FRAME
                || entity.getType() == EntityType.PAINTING
                || entity.getType() == EntityType.MINECART
                || entity.getType() == EntityType.BOAT)
                && !playerData.isAduty();
    }
}
