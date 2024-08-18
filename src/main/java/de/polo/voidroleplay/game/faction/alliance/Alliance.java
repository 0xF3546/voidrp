package de.polo.voidroleplay.game.faction.alliance;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.database.MySQL;
import de.polo.voidroleplay.game.faction.streetwar.StreetwarData;
import de.polo.voidroleplay.utils.*;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Alliance implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;
    public Alliance(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;

        Main.registerCommand("alliance", this);
    }

    @SneakyThrows
    public void endAlliance(String faction) {
        FactionData factionData = factionManager.getFactionData(faction);
        FactionData otherFaction = factionManager.getFactionData(factionData.getAllianceFaction());
        factionManager.sendCustomMessageToFactions("§8[§cBündnis§8]§c Die Fraktion §" + factionData.getPrimaryColor() + factionData.getFullname() + "§c hat das Bündnis beendet!", factionData.getName(), otherFaction.getName());
        factionData.setAllianceFaction(0);
        factionData.save();
        otherFaction.setAllianceFaction(0);
        otherFaction.save();
    }

    public void accept(Player player, String attackerFaction) {
        MySQL mySQL = Main.getInstance().mySQL;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        FactionData attackerData = factionManager.getFactionData(attackerFaction);
        factionManager.sendCustomMessageToFaction(attackerFaction, "§8[§cBündnis§8]§a Die Fraktion " + factionData.getFullname() + " hat den Bündnis-Vertrag angenommen.");
        factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§cBündnis§8]§a " + player.getName() + " hat den Bündnis-Vertrag mit " + attackerData.getFullname() + " angenommen.");
        factionData.setAllianceFaction(attackerData.getId());
        factionData.save();
        attackerData.setAllianceFaction(factionData.getId());
        attackerData.save();
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8[§c§lBÜNDNIS§8]§e Die Fraktion §" + attackerData.getPrimaryColor() + attackerData.getName() + "§e und §" + factionData.getPrimaryColor() + factionData.getName() + "§e haben ein Bündnis geschlossen!");
        Bukkit.broadcastMessage("");
    }

    public void deny(Player player, String attackerFaction) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        FactionData attackerData = factionManager.getFactionData(attackerFaction);
        factionManager.sendCustomMessageToFaction(attackerFaction, "§8[§cBündnis§8]§c Die Fraktion " + factionData.getFullname() + " hat den Bündnis-Vertrag abgelehnt.");
        factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§cBündnis§8]§c " + player.getName() + " hat den Bündnis-Vertrag gegen " + attackerData.getFullname() + " abgelehnt.");
    }

    public FactionData getAlliance(String faction) {
        for (FactionData factionData : factionManager.getFactions()) {
            if (factionData.getName().equalsIgnoreCase(faction)) {
                if (factionData.getAllianceFaction() == 0) return null;
                return factionManager.getFactionData(factionData.getAllianceFaction());
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        player.sendMessage(Prefix.ERROR + "Dieses Feature ist deaktiviert!");
        return false;
        /*if (playerData.getFactionGrade() < 7) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (!factionData.isBadFrak()) {
            player.sendMessage(Main.error + "Deine Fraktion kann keine Bündnise starten.");
            return false;
        }
        if (args.length < 1) {
            FactionData allianceFaction = getAlliance(playerData.getFaction());
            if (allianceFaction == null) {
                player.sendMessage(Main.error + "Syntax-Fehler: /alliance [Fraktion/end]");
                return false;
            }
            player.sendMessage("§8[§cBündnis§8]§7 Deine Fraktion ist mit §" + allianceFaction.getPrimaryColor() + allianceFaction.getFullname() + "§7 im Bündnis.");
            return false;
        }
        if (args[0].equalsIgnoreCase("end")) {
            endAlliance(playerData.getFaction());
            return false;
        }
        ArrayList<Player> availablePlayers = new ArrayList<>();
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playersData = playerManager.getPlayerData(players.getUniqueId());
            if (playersData.getFaction() == null) continue;
            if (playersData.getFaction().equalsIgnoreCase(args[0]) && playersData.getFactionGrade() >= 7) {
                availablePlayers.add(players);
            }
        }
        if (availablePlayers.size() == 0) {
            player.sendMessage(Main.error + "Es ist kein Fraktionsleader der anderen Partei online.");
            return false;
        }
        FactionData defenderData = factionManager.getFactionData(playerManager.getPlayerData(availablePlayers.get(0).getUniqueId()).getFaction());
        if (!defenderData.isBadFrak()) {
            player.sendMessage(Main.error + "Die Gegner-Partei kann kein Bündnis starten.");
            return false;
        }
        if (playerData.getFaction().equalsIgnoreCase(args[0])) {
            player.sendMessage(Main.error + "Du kannst dein Bündnis mit deiner eigenen Fraktion starten.");
            return false;
        }
        if (getAlliance(args[0]) != null) {
            player.sendMessage(Prefix.ERROR + "Die andere Partei ist bereits in einem Bündnis.");
        }
        boolean sendMessage = false;
        for (Player leader : availablePlayers) {
            if (player.getLocation().distance(leader.getLocation()) < 5) {
                leader.sendMessage("§8[§cBündnis§8]§c " + factionManager.getPlayerFactionRankName(player) + " " + player.getName() + " hat deine Fraktion zum Bündnis eingeladen.");
                utils.vertragUtil.sendInfoMessage(leader);
                VertragUtil.setVertrag(player, leader, "alliance", factionData.getName());
                sendMessage = true;
            }
        }
        if (!sendMessage) {
            player.sendMessage(Main.error + "Es befindet sich kein Fraktionsleader in deiner nähe.");
            return false;
        }
        player.sendMessage("§8[§cBündnis§8]§a Du hast die Fraktion zum Bündnis eingeladen.");
        return false;*/
    }
}
