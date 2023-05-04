package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class DamageListener implements Listener {
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = ((Player) event.getEntity()).getPlayer();
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            if (playerData.isAduty() || event.getEntity().getType() == EntityType.MINECART || event.getEntity().getType() == EntityType.ARMOR_STAND || event.getEntity().getType() == EntityType.PAINTING) {
                event.setCancelled(true);
            } else {
                ItemStack chestplate = player.getInventory().getArmorContents()[2];
                if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                {
                    if (chestplate.getType() == Material.LEATHER_CHESTPLATE) {
                        event.setDamage(event.getDamage() / 2);
                    } else if (chestplate.getType() == Material.IRON_CHESTPLATE) {
                        event.setDamage(event.getDamage() / 5);
                    }
                }
                event.setCancelled(false);
            }
        }
    }
}
