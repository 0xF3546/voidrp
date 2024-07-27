package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.DBPlayerData;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.FactionPlayerData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class GiveRankCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    public GiveRankCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("giverank", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFactionGrade() < 7) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Main.error + "Syntax-Fehler: /giverank [Spieler] [Rang]");
            return false;
        }
        OfflinePlayer targetplayer = Utils.getOfflinePlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Main.error + args[0] + " wurde nicht gefunden.");
            return false;
        }
        int rang;
        try {
            rang = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Main.error + "Der Rang muss eine Zahl sein!");
            return false;
        }
        if (0 > rang || rang > 8) {
            player.sendMessage(Main.error + "Der Rang muss von 0-8 sein!");
            return false;
        }
        DBPlayerData dbPlayerData = ServerManager.dbPlayerDataMap.get(targetplayer.getUniqueId().toString());
        FactionPlayerData factionPlayerData = ServerManager.factionPlayerDataMap.get(targetplayer.getUniqueId().toString());
        try {
            if (!factionPlayerData.getFaction().equals(playerData.getFaction()) || dbPlayerData.getFaction() == null) {
                player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner Fraktion.");
                return false;
            }
            if (factionPlayerData.getFaction_grade() >= playerData.getFactionGrade()) {
                player.sendMessage(Main.error_nopermission);
                return false;
            }
        } catch (Exception ex) {
            player.sendMessage(Prefix.ERROR + "Fehler beim setzen der Ränge, bitte warte bis der Server neugestartet wurde.");
        }
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        player.sendMessage("§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§7 Du hast " + targetplayer.getName() + " Rang " + rang + " gegeben!");
        factionPlayerData.setFaction_grade(rang);
        dbPlayerData.setFaction_grade(rang);
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            statement.executeUpdate("UPDATE players SET faction_grade = " + rang + " WHERE uuid = '" + targetplayer.getUniqueId() + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (targetplayer.isOnline()) {
            Player target = Bukkit.getPlayer(args[0]);
            PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
            if (targetplayerData.getFactionGrade() >= 7) {
                if (rang < 7) {
                    TeamSpeak.reloadPlayer(targetplayerData.getUuid());
                }
            }
            targetplayerData.setFactionGrade(rang);
            if (targetplayerData.getFactionGrade() >= 7) {
                TeamSpeak.reloadPlayer(targetplayer.getUniqueId());
            }
            target.sendMessage("§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§7 " + factionManager.getPlayerFactionRankName(player) + " " + player.getName() + " hat dir Rang " + rang + " gegeben!");
        }
        return false;
    }
}
