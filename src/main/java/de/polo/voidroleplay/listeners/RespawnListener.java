package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener {
    private final PlayerManager playerManager;
    public RespawnListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!playerData.isDead()) {
            return;
        }
        if (playerData.getVariable("current_lobby") != null) {
            return;
        }
        event.setRespawnLocation(playerData.getDeathLocation());
        player.sendTitle("§cDu bist gestorben.", null, 1, 12, 1);
        player.setGameMode(GameMode.SPECTATOR);
        player.setFlySpeed(0);
    }
}
