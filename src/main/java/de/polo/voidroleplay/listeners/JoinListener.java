package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.RankData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.Interfaces.PlayerJoin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.UUID;

public class JoinListener implements Listener {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final Utils utils;
    private final LocationManager locationManager;
    private final ServerManager serverManager;
    private PlayerJoin playerJoin;
    public JoinListener(PlayerManager playerManager, AdminManager adminManager, Utils utils, LocationManager locationManager, ServerManager serverManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.utils = utils;
        this.locationManager = locationManager;
        this.serverManager = serverManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        event.setJoinMessage("");
        if (playerManager.isCreated(player.getUniqueId())) {
            playerManager.loadPlayer(player);
            PlayerData playerData = playerManager.getPlayerData(uuid);
            adminManager.send_message(player.getName() + " hat den Server betreten.", ChatColor.GRAY);
            player.sendMessage("§6Willkommen zurück, " + player.getName() + "!");
            RankData rankData = ServerManager.rankDataMap.get(playerData.getRang());
            Utils.Tablist.setTablist(player, null);
            playerData.setUuid(player.getUniqueId());
            if (playerData.getPermlevel() >= 40) {
                utils.sendActionBar(player, "§aDeine Account-Daten wurden erfolgreich geladen!");
                player.sendMessage("§8 ➥ §cEs sind " + SupportManager.playerTickets.size() + " Tickets offen.");
                int teamCount = 0;
                int deathCount = 0;
                for (Player player1 : Bukkit.getOnlinePlayers()) {
                    PlayerData playerData1 = playerManager.getPlayerData(player1.getUniqueId());
                    if (playerData1.getPermlevel() >= 40) {
                        teamCount++;
                    }
                    if (playerData1.isDead()) {
                        deathCount++;
                    }
                }
                player.sendMessage("§8 ➥ §cEs sind " + Bukkit.getOnlinePlayers().size() + " Spieler online §7(§c" + (teamCount - 1) + " weitere Teammitglieder§7)§c.");
                player.sendMessage("§8     ➥ §cEs sind " + deathCount + " Spieler bewusstlos.");
            }
            Vehicles.spawnPlayerVehicles(player);
            serverManager.updateTablist(null);
        } else {
            player.sendMessage(" ");
            player.sendMessage("§6VoidRoleplay §8»§7 Herzlich Wilkommen auf VoidRoleplay, " + player.getName() + ".");
            player.sendMessage(" ");
            locationManager.useLocation(player, "Spawn");
            adminManager.send_message("§c" + player.getName() + "§7 hat sich gerade registriert.", ChatColor.GREEN);
        }
        Utils.Tablist.updatePlayer(player);
    }
}
