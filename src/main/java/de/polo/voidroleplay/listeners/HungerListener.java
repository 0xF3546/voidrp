package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class HungerListener implements Listener {
    private final PlayerManager playerManager;

    public HungerListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        //event.setCancelled(true);
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.isAduty()) {
            event.setCancelled(true);
        }
    }
}
