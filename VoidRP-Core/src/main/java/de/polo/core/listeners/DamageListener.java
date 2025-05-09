package de.polo.core.listeners;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.enums.RoleplayItem;
import de.polo.core.utils.Event;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import static de.polo.core.Main.playerManager;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_ATTACK;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.PROJECTILE;

@Event
public class DamageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() == EntityType.ARMOR_STAND
                || event.getEntity().getType() == EntityType.ITEM_FRAME
                || event.getEntity().getType() == EntityType.PAINTING
                || event.getEntity().getType() == EntityType.MINECART) {
            event.setCancelled(true);
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = ((Player) event.getEntity()).getPlayer();
        if (player == null) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        // ISSUE VRP-10000: fixed by adding null check
        if (playerData != null) {
            VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
            if (voidPlayer.isAduty() || playerData.isAFK() || Main.getInstance().supportManager.isInAcceptedTicket(player) || playerData.isDead() || playerData.isCuffed()) {
                event.setCancelled(true);
                return;
            }
        }
        player.getWorld().playEffect(player.getLocation().add(0, 0.5, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        if (event.getCause() == PROJECTILE) {
            if (player.getInventory().getItemInMainHand().getType() == RoleplayItem.SWAT_SHIELD.getMaterial()) {
                if (!player.isBlocking()) return;
                ItemStack shield = player.getInventory().getItemInMainHand();
                ItemMeta meta = shield.getItemMeta();
                if (meta instanceof Damageable damageable) {
                    damageable.setDamage(damageable.getDamage() + 15);
                    shield.setItemMeta(meta);
                }
            }
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor == null || armor.getType() == Material.AIR) continue;
                event.setDamage(0.5);
                ItemMeta meta = armor.getItemMeta();
                if (meta instanceof Damageable damageable) {
                    if (armor.getType().equals(RoleplayItem.HEAVY_BULLETPROOF.getMaterial())) {
                        damageable.setDamage(damageable.getDamage() + 30);
                    } else {
                        damageable.setDamage(damageable.getDamage() + 15);
                    }
                    armor.setItemMeta(meta);
                }
            }
        }
        if (event.getCause() == ENTITY_ATTACK) {
            // ISSUE VRP-10003: Added null check for playerData
            event.setCancelled(playerData != null && playerData.getVisum() < 2 && playerData.getFaction() == null);
        }
    }
}
