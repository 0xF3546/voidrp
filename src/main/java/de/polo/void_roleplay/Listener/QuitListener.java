package de.polo.void_roleplay.Listener;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.SupportManager;
import de.polo.void_roleplay.Utils.Vehicles;
import de.polo.void_roleplay.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class QuitListener implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = (Player) event.getPlayer();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData == null) return;
        event.setQuitMessage("");
        aduty.send_message("§c" + player.getName() + "§7 hat den Server verlassen.");
        if (player.getVehicle() != null) {
            player.getVehicle().eject();
        }
        try {
            Vehicles.deleteVehicleByUUID(player.getUniqueId().toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            if (playerData.getVariable("job") != null) {
                switch (playerData.getVariable("job")) {
                    case "lumberjack":
                        lumberjackCommand.quitJob(player);
                        break;
                    case "apfelsammler":
                        apfelplantageCommand.quitJob(player);
                        break;
                    case "mine":
                        mineCommand.quitJob(player);
                        break;
                    case "lieferant":
                        lebensmittellieferantCommand.quitJob(player);
                        break;
                }
            }
            PlayerManager.savePlayer(player);
            SupportManager.deleteTicket(player);
            if (SupportManager.isInConnection(player)) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (SupportManager.getConnection(players).equalsIgnoreCase(player.getUniqueId().toString()) || SupportManager.getConnection(player).equalsIgnoreCase(players.getUniqueId().toString())) {
                        SupportManager.deleteTicketConnection(players, player);
                        SupportManager.deleteTicketConnection(player, players);
                        players.sendMessage(Main.support_prefix + "§c" + player.getName() + "§7 ist offline gegangen. Das Ticket wurde geschlossen.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}