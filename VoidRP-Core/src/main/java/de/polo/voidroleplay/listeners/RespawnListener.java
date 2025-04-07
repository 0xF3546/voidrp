package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.gameplay.MilitaryDrop;
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
        if (playerData == null) return;
        float flySpeed = 0;
        if (event.isBedSpawn()) {
            event.setRespawnLocation(playerData.getDeathLocation());
        }
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
        player.sendTitle("§cDu bist gestorben.", null, 1, 12, 1);
        // player.setGameMode(GameMode.SPECTATOR);
        player.setFlySpeed(flySpeed);
        if (playerData.getKarma() < -50) {
            event.setRespawnLocation(Main.getInstance().locationManager.getLocation("hell"));
            Main.getInstance().locationManager.useLocation(player,"hell");
        } else if (playerData.getKarma() >= 50) {
            event.setRespawnLocation(Main.getInstance().locationManager.getLocation("heaven"));
            Main.getInstance().locationManager.useLocation(player,"heaven");
        } else {
            event.setRespawnLocation(Main.getInstance().locationManager.getLocation("cemetery"));
            Main.getInstance().locationManager.useLocation(player, "cemetery");
        }
    }
}
