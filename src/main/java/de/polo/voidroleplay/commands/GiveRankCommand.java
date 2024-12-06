package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.FactionPlayerData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.TeamSpeak;
import de.polo.voidroleplay.utils.Utils;
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
        if (playerData.getFactionGrade() < 5) {
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
        if (0 > rang || rang > 6) {
            player.sendMessage(Main.error + "Der Rang muss von 0-6 sein!");
            return false;
        }
        PlayerData targetData = factionManager.getFactionOfPlayer(targetplayer.getUniqueId());
        try {
            if (!targetData.getFaction().equals(playerData.getFaction()) || targetData.getFaction() == null) {
                player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner Fraktion.");
                return false;
            }
            if (targetData.getFactionGrade() >= playerData.getFactionGrade()) {
                player.sendMessage(Main.error_nopermission);
                return false;
            }
        } catch (Exception ex) {
            player.sendMessage(Prefix.ERROR + "Fehler beim setzen der Ränge, bitte warte bis der Server neugestartet wurde.");
        }
        int leaders = 0;
        if (rang >= 7) {
            for (FactionPlayerData fpd : factionManager.getFactionMember(playerData.getFaction())) {
                if (fpd.getFaction_grade() >= 7) leaders++;
            }
        }
        int leaderLimit = 3;
        if (playerData.getFaction().equalsIgnoreCase("Medic") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
            leaderLimit = 4;
        }
        if (leaders >= leaderLimit) {
            player.sendMessage(Prefix.ERROR + "Deine Fraktion kann nur " + leaderLimit + " Leader haben!");
            return false;
        }
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        player.sendMessage("§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§7 Du hast " + targetplayer.getName() + " Rang " + rang + " gegeben!");
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
            statement.executeUpdate("UPDATE players SET faction_grade = " + rang + " WHERE uuid = '" + targetplayer.getUniqueId() + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (targetplayer.isOnline()) {
            Player target = Bukkit.getPlayer(args[0]);
            PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
            if (targetplayerData.getFactionGrade() >= 5) {
                if (rang < 7) {
                    TeamSpeak.reloadPlayer(targetplayerData.getUuid());
                }
            }
            targetplayerData.setFactionGrade(rang);
            if (targetplayerData.getFactionGrade() >= 5) {
                TeamSpeak.reloadPlayer(targetplayer.getUniqueId());
            }
            target.sendMessage("§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§7 " + factionManager.getPlayerFactionRankName(player) + " " + player.getName() + " hat dir Rang " + rang + " gegeben!");
        }
        return false;
    }
}
