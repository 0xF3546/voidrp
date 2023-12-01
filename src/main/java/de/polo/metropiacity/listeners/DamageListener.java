package de.polo.metropiacity.listeners;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class DamageListener implements Listener {
    private final PlayerManager playerManager;
    public DamageListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = ((Player) event.getEntity()).getPlayer();
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            ItemStack chestplate = player.getInventory().getArmorContents()[2];
            if (!playerData.isAduty()) {
                player.getWorld().playEffect(player.getLocation().add(0, 0.5, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
                if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                    event.setCancelled(playerManager.getPlayerData(player.getUniqueId()).getVisum() <= 2);
                    if (chestplate.getType() == Material.LEATHER_CHESTPLATE) {
                        event.setDamage(event.getDamage() / 1.2);
                    }
                }
            } else {
                event.setCancelled(true);
            }
        }
        if ((event.getEntity().getType() == EntityType.ARMOR_STAND || event.getEntity().getType() == EntityType.ITEM_FRAME || event.getEntity().getType() == EntityType.PAINTING || event.getEntity().getType() == EntityType.MINECART) && !playerManager.getPlayerData(event.getEntity().getUniqueId()).isAduty()) {
            event.setCancelled(true);
        }
        if (event.getEntity().getType() == EntityType.VILLAGER) {
            String command = event.getEntity().getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "command"), PersistentDataType.STRING);
            if (command == null) return;
            event.setCancelled(true);
        }
    }
}
