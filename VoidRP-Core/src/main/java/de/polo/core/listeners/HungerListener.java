package de.polo.core.listeners;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.Random;

public class HungerListener implements Listener {
    /**
     * The odds of the player's food level being reduced
     */
    public static final double ODDS = 0.4;
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
        VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (voidPlayer.isAduty() || playerData.isDead()) {
            event.setCancelled(true);
            return;
        }

        Random random = new Random();
        int currentFoodLevel = player.getFoodLevel();
        int difference = currentFoodLevel - event.getFoodLevel();

        if (difference > 0) {
            // If the random number is less than the odds, the player's food level will not be reduced
            if (random.nextDouble() < ODDS) {
                event.setFoodLevel(currentFoodLevel);
            } else {
                event.setFoodLevel(currentFoodLevel - difference);
            }
        }
    }
}
