package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.Random;

public class HungerListener implements Listener {
    private final PlayerManager playerManager;

    public HungerListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        //event.setCancelled(true);
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.isAduty()) {
            event.setCancelled(true);
            return;
        }

        Random random = new Random();
        int currentFoodLevel = player.getFoodLevel();
        int difference = currentFoodLevel - event.getFoodLevel();

        if (difference > 0) {
            // reduction of 50% based on the odds
            if (random.nextBoolean()) {
                event.setFoodLevel(currentFoodLevel);
            } else {
                event.setFoodLevel(currentFoodLevel - difference);
            }
        }
    }
}
