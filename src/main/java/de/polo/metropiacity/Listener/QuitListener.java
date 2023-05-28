package de.polo.metropiacity.Listener;

import de.polo.metropiacity.DataStorage.ServiceData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.PlayerUtils.ChatUtils;
import de.polo.metropiacity.PlayerUtils.DeathUtil;
import de.polo.metropiacity.PlayerUtils.FFA;
import de.polo.metropiacity.Utils.*;
import de.polo.metropiacity.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
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
        ServerManager.updateTablist(null);
        if (playerData.getVariable("current_lobby") != null) {
            FFA.leaveFFA(player);
        }
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
                        lumberjackCommand.quitJob(player, true);
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
                    case "farmer":
                        farmerCommand.quitJob(player);
                        break;
                    case "Postbote":
                        postboteCommand.quitJob(player, true);
                        break;
                    case "Müllmann":
                        muellmannCommand.quitJob(player, true);
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
            ServiceData serviceData = StaatUtil.serviceDataMap.get(player.getUniqueId().toString());
            if (serviceData != null) {
                StaatUtil.cancelservice(player);
            }
            Item item = DeathUtil.deathSkulls.get(player.getUniqueId().toString());
            item.remove();
            DeathUtil.deathSkulls.remove(player.getUniqueId().toString());
            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat den Server verlassen (" + event.getQuitMessage() + ").");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}