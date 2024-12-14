package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
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

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_ATTACK;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.PROJECTILE;

public class DamageListener implements Listener {
    private final PlayerManager playerManager;

    public DamageListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

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
            if (playerData.isAduty() || playerData.isAFK() || Main.getInstance().supportManager.getTicket(player) != null || playerData.isDead()) {
                event.setCancelled(true);
                return;
            }
        }
        player.getWorld().playEffect(player.getLocation().add(0, 0.5, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        if (event.getCause() == PROJECTILE) {
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor == null || armor.getType() == Material.AIR) continue;
                event.setDamage(0.5);
                ItemMeta meta = armor.getItemMeta();
                if (meta instanceof Damageable) {
                    Damageable damageable = (Damageable) meta;
                    if (armor.getType().equals(RoleplayItem.HEAVY_BULLETPROOF.getMaterial())) {
                        damageable.setDamage(damageable.getDamage() + 30);
                    } else {
                        damageable.setDamage(damageable.getDamage() + 15);
                    }
                    armor.setItemMeta(meta);
                }
            }
            if (player.getInventory().getItemInMainHand().getType() == RoleplayItem.SWAT_SHIELD.getMaterial()) {
                if (!player.isBlocking()) return;
                ItemStack shield = player.getInventory().getItemInMainHand();
                ItemMeta meta = shield.getItemMeta();
                if (meta instanceof Damageable) {
                    Damageable damageable = (Damageable) meta;
                    damageable.setDamage(damageable.getDamage() + 15);
                    shield.setItemMeta(meta);
                }
            }
        }
        if (event.getCause() == ENTITY_ATTACK) {
            event.setCancelled(playerData.getVisum() <= 2 && playerData.getFaction() == null);
        }
    }
}
