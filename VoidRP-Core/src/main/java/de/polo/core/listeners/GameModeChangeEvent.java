package de.polo.core.listeners;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Utils;
import de.polo.core.utils.Event;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

@Event
public class GameModeChangeEvent implements Listener {
    public GameModeChangeEvent() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player);
        // ISSUE VRP-10004: fixed by adding null check
        if (playerData == null) return;
        if (event.getNewGameMode() == GameMode.SURVIVAL) {
            playerData.removeBossBar("buildMode");
            if (playerData.getVariable("inventory::build") == null) return;
            player.getInventory().setContents(playerData.getVariable("inventory::build"));
            playerData.setVariable("inventory::build", null);
        }
        if (event.getNewGameMode() == GameMode.CREATIVE) {
            BossBar bossBar = Bukkit.createBossBar("§8⚒ §2Du befindest dich im Baumodus §8⚒", BarColor.GREEN, BarStyle.SOLID);
            playerData.setBossBar("buildMode", bossBar);
            playerData.setVariable("inventory::build", player.getInventory().getContents());
            Utils.Tablist.setTablist(player, "§8[§2GM§8]");
            player.getInventory().clear();
            return;
        }
        Utils.Tablist.setTablist(player, null);
    }
}
