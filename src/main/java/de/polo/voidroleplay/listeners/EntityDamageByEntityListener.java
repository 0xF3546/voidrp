package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.PlayerManager;
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
            PlayerData playerData = playerManager.getPlayerData(event.getDamager().getUniqueId());
            if (playerData.getVisum() <= 2 && playerData.getFaction() == null) {
                event.setCancelled(true);
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
