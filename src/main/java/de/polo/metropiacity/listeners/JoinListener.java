package de.polo.metropiacity.listeners;

import de.polo.metropiacity.playerUtils.DeathUtils;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.*;
import de.polo.metropiacity.commands.ADutyCommand;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

public class JoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) throws SQLException {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        event.setJoinMessage("");
        if (PlayerManager.isCreated(String.valueOf(player.getUniqueId()))) {
            PlayerManager.loadPlayer(player);
            PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
            if (DeathUtils.isDead(player)) {
                DeathUtils.killPlayer(player);
            }
            ADutyCommand.send_message(player.getName() + " hat den Server betreten.");
            player.sendMessage("§6Willkommen zurück, " + player.getName() + "!");
            if (playerData.getPermlevel() >= 40) {
                Utils.sendActionBar(player, "§aDeine Account-Daten wurden erfolgreich geladen!");
                player.sendMessage("§8 ➥ §cEs sind " + SupportManager.playerTickets.size() + " Tickets offen.");
                int teamCount = 0;
                int deathCount = 0;
                for (Player player1 : Bukkit.getOnlinePlayers()) {
                    PlayerData playerData1 = PlayerManager.playerDataMap.get(player1.getUniqueId().toString());
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
            ServerManager.updateTablist(null);
        } else {
            player.sendMessage(" ");
            player.sendMessage("§6MetropiaCity §8»§7 Herzlich Wilkommen in Metropia - der Stadt mit Zukunft, " + player.getName() + ".");
            player.sendMessage(" ");
            LocationManager.useLocation(player, "Spawn");
            player.getWorld().playEffect(player.getLocation().add(0.0D, 0.0D, 0.0D), Effect.ENDER_SIGNAL, 1);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1,2);
            ADutyCommand.send_message("§c" + player.getName() + "§7 hat sich gerade registriert.");
        }
    }
}
