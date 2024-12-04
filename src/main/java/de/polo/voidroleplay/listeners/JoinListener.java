package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.RankData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.game.base.vehicle.Vehicles;
import de.polo.voidroleplay.game.faction.plants.Plant;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.Interfaces.PlayerJoin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
        player.setGameMode(GameMode.SURVIVAL);
        if (playerManager.isCreated(player.getUniqueId())) {
            PlayerData playerData = playerManager.loadPlayer(player).join();
                if (playerData == null) {
                    player.kick(Component.text("§cWir mussten deine Verbindung trennen, da deine Spielerdaten nicht geladen werden konnten."));
                    return;
                }
                System.out.println(playerData);
                System.out.println(playerData.getPlayer().getName());
                System.out.println(playerData.getFirstname());
                adminManager.send_message(player.getName() + " hat den Server betreten.", ChatColor.GRAY);
                player.sendMessage("§6Willkommen zurück, " + player.getName() + "!");
                if (playerData.getFaction() != null) {
                    FactionData factionData = Main.getInstance().factionManager.getFactionData(playerData.getFaction());
                    player.sendMessage("§8 ➥ §6[FMOTD] " + factionData.getMotd());
                }
                RankData rankData = ServerManager.rankDataMap.get(playerData.getRang());
                Utils.Tablist.setTablist(player, null);
                playerData.setUuid(player.getUniqueId());
                if (playerData.getPermlevel() >= 40) {
                    player.sendMessage("§8 ➥ §cEs sind " + Main.getInstance().supportManager.getTickets().size() + " Tickets offen.");
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
        if (player.getGameMode() == GameMode.CREATIVE) {
            Utils.Tablist.setTablist(player, "§8[§2GM§8]");
        } else {
            Utils.Tablist.setTablist(player, null);
        }
    }
}
