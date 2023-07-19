package de.polo.metropiacity.listeners;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class DamageListener implements Listener {
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = ((Player) event.getEntity()).getPlayer();
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            ItemStack chestplate = player.getInventory().getArmorContents()[2];
            if (!playerData.isAduty()) {
                if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                    event.setCancelled(PlayerManager.playerDataMap.get(player.getUniqueId().toString()).getVisum() <= 2);
                    if (chestplate.getType() == Material.LEATHER_CHESTPLATE) {
                        event.setDamage(event.getDamage() / 2);
                    } else if (chestplate.getType() == Material.IRON_CHESTPLATE) {
                        event.setDamage(event.getDamage() / 5);
                    }
                }
            } else {
                event.setCancelled(true);
            }
        }
        if ((event.getEntity().getType() == EntityType.ARMOR_STAND || event.getEntity().getType() == EntityType.ITEM_FRAME || event.getEntity().getType() == EntityType.PAINTING || event.getEntity().getType() == EntityType.MINECART) && !PlayerManager.playerDataMap.get(event.getEntity().getUniqueId().toString()).isAduty()) {
            event.setCancelled(true);
        }
        if (event.getEntity().getType() == EntityType.VILLAGER) {
            String command = event.getEntity().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "command"), PersistentDataType.STRING);
            if (command == null) return;
            event.setCancelled(true);
        }
    }
}
