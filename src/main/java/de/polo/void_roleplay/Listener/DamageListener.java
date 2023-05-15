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
                ItemStack chestplate = player.getInventory().getArmorContents()[2];
                if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                {
                    event.setCancelled(PlayerManager.playerDataMap.get(player.getUniqueId().toString()).getVisum() <= 2);
                    if (chestplate.getType() == Material.LEATHER_CHESTPLATE) {
                        event.setDamage(event.getDamage() / 2);
                    } else if (chestplate.getType() == Material.IRON_CHESTPLATE) {
                        event.setDamage(event.getDamage() / 5);
                    }
                }
        }
        if ((event.getEntity().getType() == EntityType.ARMOR_STAND || event.getEntity().getType() == EntityType.ITEM_FRAME || event.getEntity().getType() == EntityType.PAINTING || event.getEntity().getType() == EntityType.MINECART) && !PlayerManager.playerDataMap.get(event.getEntity().getUniqueId().toString()).isAduty()) {
            event.setCancelled(true);
        }
        if (event.getEntity().getType() == EntityType.VILLAGER) {
            event.setCancelled(true);
        }
    }
}
