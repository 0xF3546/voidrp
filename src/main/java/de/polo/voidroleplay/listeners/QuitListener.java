package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.dataStorage.ServiceData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.Ticket;
import de.polo.voidroleplay.game.base.vehicle.Vehicles;
import de.polo.voidroleplay.utils.GamePlay.MilitaryDrop;
import de.polo.voidroleplay.utils.Interfaces.PlayerQuit;
import de.polo.voidroleplay.utils.enums.PlayerPed;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.playerUtils.ScoreboardAPI;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;
import java.sql.Statement;

public class QuitListener implements Listener {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final Utils utils;
    private final Main.Commands commands;
    private final ServerManager serverManager;
    private final SupportManager supportManager;
    private PlayerQuit playerQuit;
    private final ScoreboardAPI scoreboardAPI;
    public QuitListener(PlayerManager playerManager, AdminManager adminManager, Utils utils, Main.Commands commands, ServerManager serverManager, SupportManager supportManager, ScoreboardAPI scoreboardAPI) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.utils = utils;
        this.commands = commands;
        this.serverManager = serverManager;
        this.supportManager = supportManager;
        this.scoreboardAPI = scoreboardAPI;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage("");
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return;
        scoreboardAPI.clearScoreboards(player);
        adminManager.send_message(player.getName() + " hat den Server verlassen.", ChatColor.GRAY);
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            player.setGameMode(GameMode.SURVIVAL);
        }
        if (MilitaryDrop.ACTIVE) Main.getInstance().gamePlay.militaryDrop.handleQuit(player);
        serverManager.updateTablist(null);
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            playerData.setVariable("inventory::build", player.getInventory().getContents());
        }
        if (playerData.getVariable("current_lobby") != null) {
            utils.ffaUtils.leaveFFA(player);
        }
        if (playerData.getVariable("gangwar") != null) {
            utils.gangwarUtils.leaveGangwar(player);
        }
        if (player.getVehicle() != null) {
            player.getVehicle().eject();
        }
        if (playerData.isCuffed()) {
            player.setWalkSpeed(0.2F);
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
            player.removePotionEffect(PotionEffectType.JUMP);
            player.removePotionEffect(PotionEffectType.SLOW);
        }
        PlayerPed ped = playerData.getPlayerPetManager().getActivePed();
        if (ped != null) {
            playerData.getPlayerPetManager().despawnPet(ped);
        }
        try {
            Vehicles.deleteVehicleByUUID(player.getUniqueId().toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
            Ticket ticket = supportManager.getTicket(player);
            if (ticket != null) {
                for (Player p : supportManager.getPlayersInTicket(ticket)) {
                    if (p.isOnline()) {
                        p.sendMessage(Main.support_prefix + player.getName() + " hat den Server verlassen, das Ticket wurde geschlossen.");
                    }
                }
                supportManager.removeTicket(ticket);
            }
            ServiceData serviceData = StaatUtil.serviceDataMap.get(player.getUniqueId().toString());
            if (serviceData != null) {
                utils.staatUtil.cancelService(player);
            }
            ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " hat den Server verlassen (" + event.getQuitMessage() + ").");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}