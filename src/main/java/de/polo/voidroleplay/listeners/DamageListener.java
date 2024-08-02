package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.*;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        if (event.getEntity() instanceof Player) {
            Player player = ((Player) event.getEntity()).getPlayer();
            if (player == null) return;
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            ItemStack chestplate = player.getInventory().getArmorContents()[2];
            if (!playerData.isAduty() || Main.getInstance().supportManager.getTicket(player) != null) {
                player.getWorld().playEffect(player.getLocation().add(0, 0.5, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
                if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                    event.setCancelled(playerData.getVisum() <= 2 && playerData.getFaction() == null);
                    if (chestplate == null) return;
                    if (chestplate.getType() == Material.LEATHER_CHESTPLATE) {
                        event.setDamage(event.getDamage() / 3);
                        chestplate.setDurability((short) (chestplate.getDurability() - 10));
                    }
                }
            } else {
                event.setCancelled(true);
            }
        }
    }
}
