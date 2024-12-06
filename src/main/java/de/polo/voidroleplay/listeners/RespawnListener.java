package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.GamePlay.MilitaryDrop;
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
        float flySpeed = 0;
        if (!playerData.isDead()) {
            if (MilitaryDrop.ACTIVE) {
                if (!Main.getInstance().gamePlay.militaryDrop.isPlayerInEvent(player)) {
                    return;
                } else {
                    flySpeed = 0.1F;
                }
            }
        }
        /*if (playerData.getVariable("ffa") != null) {
            Main.getInstance().gamePlay.getFfa().handleDeath(player);
            return;
        }*/
        event.setRespawnLocation(playerData.getDeathLocation());
        player.sendTitle("§cDu bist gestorben.", null, 1, 12, 1);
        player.setGameMode(GameMode.SPECTATOR);
        player.setFlySpeed(flySpeed);
    }
}
