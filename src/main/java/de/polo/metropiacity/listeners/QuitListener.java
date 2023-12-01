package de.polo.metropiacity.listeners;

import de.polo.metropiacity.dataStorage.ServiceData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
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
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final Utils utils;
    private final Main.Commands commands;
    private final ServerManager serverManager;
    private final SupportManager supportManager;
    public QuitListener(PlayerManager playerManager, AdminManager adminManager, Utils utils, Main.Commands commands, ServerManager serverManager, SupportManager supportManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.utils = utils;
        this.commands = commands;
        this.serverManager = serverManager;
        this.supportManager = supportManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return;
        event.setQuitMessage("");
        adminManager.send_message(player.getName() + " hat den Server verlassen.", ChatColor.GRAY);
        serverManager.updateTablist(null);
        if (playerData.getVariable("current_lobby") != null) {
            utils.ffaUtils.leaveFFA(player);
        }
        if (playerData.getVariable("gangwar") != null) {
            utils.gangwarUtils.leaveGangwar(player);
        }
        if (player.getVehicle() != null) {
            player.getVehicle().eject();
        }
        try {
            Vehicles.deleteVehicleByUUID(player.getUniqueId().toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (utils.deathUtil.getDeathSkull(player.getUniqueId().toString()) != null) {
            Item skull = utils.deathUtil.getDeathSkull(player.getUniqueId().toString());
            skull.remove();
            utils.deathUtil.removeDeathSkull(player.getUniqueId().toString());
            playerManager.setPlayerMove(player, true);
        }
        try {
            if (playerData.getVariable("job") != null) {
                switch (playerData.getVariable("job").toString()) {
                    case "lumberjack":
                        commands.lumberjackCommand.quitJob(player, true);
                        break;
                    case "apfelsammler":
                        commands.apfelplantageCommand.quitJob(player);
                        break;
                    case "mine":
                        commands.mineCommand.quitJob(player);
                        break;
                    case "lieferant":
                        commands.lebensmittelLieferantCommand.quitJob(player);
                        break;
                    case "farmer":
                        commands.farmerCommand.quitJob(player);
                        break;
                    case "Postbote":
                        commands.postboteCommand.quitJob(player, true);
                        break;
                    case "Müllmann":
                        commands.muellmannCommand.quitJob(player, true);
                        break;
                }
            }
            playerManager.savePlayer(player);
            supportManager.deleteTicket(player);
            if (supportManager.isInConnection(player)) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (supportManager.getConnection(players).equalsIgnoreCase(player.getUniqueId().toString()) || supportManager.getConnection(player).equalsIgnoreCase(players.getUniqueId().toString())) {
                        supportManager.deleteTicketConnection(players, player);
                        supportManager.deleteTicketConnection(player, players);
                        players.sendMessage(Main.support_prefix + "§c" + player.getName() + "§7 ist offline gegangen. Das Ticket wurde geschlossen.");
                    }
                }
            }
            ServiceData serviceData = StaatUtil.serviceDataMap.get(player.getUniqueId().toString());
            if (serviceData != null) {
                utils.staatUtil.cancelService(player);
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