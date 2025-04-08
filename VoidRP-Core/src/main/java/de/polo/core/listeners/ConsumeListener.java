package de.polo.core.listeners;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.game.base.extra.PlayerIllness;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.api.player.enums.IllnessType;
import de.polo.core.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class ConsumeListener implements Listener {

    private final PlayerManager playerManager;

    public ConsumeListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        int random = Utils.random(1, 100);
        Player player = event.getPlayer();

        if (item.getType() == Material.POTION) {
            if (random > 80) {
                PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
                playerData.addIllness(new PlayerIllness(IllnessType.CHOLERA), true);
            }
        }
    }
}
