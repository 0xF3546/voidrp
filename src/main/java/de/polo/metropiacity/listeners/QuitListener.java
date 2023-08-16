package de.polo.metropiacity.listeners;

import de.polo.metropiacity.dataStorage.ServiceData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.playerUtils.ChatUtils;
import de.polo.metropiacity.playerUtils.DeathUtils;
import de.polo.metropiacity.playerUtils.FFAUtils;
import de.polo.metropiacity.utils.Game.GangwarUtils;
import de.polo.metropiacity.utils.*;
import de.polo.metropiacity.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.sql.Statement;

public class QuitListener implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData == null) return;
        event.setQuitMessage("");
        ADutyCommand.send_message(player.getName() + " hat den Server verlassen.", ChatColor.GRAY);
        ServerManager.updateTablist(null);
        if (playerData.getVariable("current_lobby") != null) {
            FFAUtils.leaveFFA(player);
        }
        if (playerData.getVariable("gangwar") != null) {
            GangwarUtils.leaveGangwar(player);
        }
        if (player.getVehicle() != null) {
            player.getVehicle().eject();
        }
        try {
            Vehicles.deleteVehicleByUUID(player.getUniqueId().toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (DeathUtils.deathSkulls.get(player.getUniqueId().toString()) != null) {
            Item skull = DeathUtils.deathSkulls.get(player.getUniqueId().toString());
            skull.remove();
            DeathUtils.deathSkulls.remove(player.getUniqueId().toString());
            PlayerManager.setPlayerMove(player, true);
        }
        try {
            if (playerData.getVariable("job") != null) {
                switch (playerData.getVariable("job")) {
                    case "lumberjack":
                        LumberjackCommand.quitJob(player, true);
                        break;
                    case "apfelsammler":
                        ApfelplantageCommand.quitJob(player);
                        break;
                    case "mine":
                        MineCommand.quitJob(player);
                        break;
                    case "lieferant":
                        LebensmittelLieferantCommand.quitJob(player);
                        break;
                    case "farmer":
                        FarmerCommand.quitJob(player);
                        break;
                    case "Postbote":
                        PostboteCommand.quitJob(player, true);
                        break;
                    case "Müllmann":
                        MuellmannCommand.quitJob(player, true);
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
            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat den Server verlassen (" + event.getQuitMessage() + ").");
            if (playerData.getVariable("tutorial") != null) {
                Statement s = Main.getInstance().mySQL.getStatement();
                s.executeUpdate("UPDATE players SET firstname = null, lastname = null, birthday = null, gender = null WHERE uuid = '" + player.getUniqueId() + "'");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}