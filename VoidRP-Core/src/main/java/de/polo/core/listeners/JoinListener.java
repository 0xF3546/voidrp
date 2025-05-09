package de.polo.core.listeners;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.faction.entity.Faction;
import de.polo.core.location.services.LocationService;
import de.polo.core.player.entities.CoreVoidPlayer;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Event;
import de.polo.core.utils.Utils;
import de.polo.core.vehicles.services.VehicleService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

import static de.polo.core.Main.playerManager;
import static de.polo.core.Main.serverManager;

@Event
public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        event.joinMessage(Component.text(""));
        player.setGameMode(GameMode.SURVIVAL);
        AdminService adminService = VoidAPI.getService(AdminService.class);
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (playerManager.isCreated(player.getUniqueId())) {
            playerManager.loadPlayer(player);
            PlayerData playerData = playerManager.getPlayerData(uuid);
            if (playerData == null) {
                player.kick(Component.text("§cWir mussten deine Verbindung trennen, da deine Spielerdaten nicht geladen werden konnten."));
                return;
            }
            adminService.sendMessage(player.getName() + " hat den Server betreten.", Color.SILVER);
            player.sendMessage("§6Willkommen zurück, " + player.getName() + "!");
            if (playerData.getFaction() != null) {
                Faction factionData = Main.getInstance().factionManager.getFactionData(playerData.getFaction());
                player.sendMessage("§8 ➥ §6[FMOTD] " + factionData.getMotd());
            }
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
            VehicleService vehicleService = VoidAPI.getService(VehicleService.class);
            vehicleService.spawnPlayerVehicles(player);
            serverManager.updateTablist(null);
        } else {
            player.sendMessage(" ");
            player.sendMessage("§6VoidRoleplay §8»§7 Herzlich willkommen auf VoidRoleplay, " + player.getName() + ".");
            player.sendMessage(" ");
            locationService.useLocation(player, "Spawn");
            adminService.sendMessage("§c" + player.getName() + "§7 hat sich gerade registriert.", Color.GREEN);
        }
        if (player.getGameMode() == GameMode.CREATIVE) {
            Utils.Tablist.setTablist(player, "§8[§2GM§8]");
        } else {
            Utils.Tablist.setTablist(player, null);
        }

        VoidAPI.addPlayer(new CoreVoidPlayer(player));
    }
}
