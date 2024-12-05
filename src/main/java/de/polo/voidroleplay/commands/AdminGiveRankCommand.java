package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.DBPlayerData;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.FactionPlayerData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.ServerManager;
import de.polo.voidroleplay.utils.TeamSpeak;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.Statement;

public class AdminGiveRankCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    public AdminGiveRankCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("admingiverank", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 90) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Main.error + "Syntax-Fehler: /agiverank [Spieler] [Rang]");
            return false;
        }
        OfflinePlayer targetplayer = Bukkit.getPlayer(args[0]);
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
        DBPlayerData dbPlayerData = ServerManager.dbPlayerDataMap.get(targetplayer.getUniqueId().toString());
        FactionPlayerData factionPlayerData = ServerManager.factionPlayerDataMap.get(targetplayer.getUniqueId().toString());
        player.sendMessage("§8[§cAdmin§8]§7 Du hast " + targetplayer.getName() + " Rang " + rang + " gegeben!");
        if (factionPlayerData != null)  {
            factionPlayerData.setFaction_grade(rang);
        }
        if (dbPlayerData != null) {
            dbPlayerData.setFaction_grade(rang);
        }
        try {
            Statement statement = Main.getInstance().getMySQL().getStatement();
            statement.executeUpdate("UPDATE players SET faction_grade = " + rang + " WHERE uuid = '" + targetplayer.getUniqueId() + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (targetplayer.isOnline()) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                return false;
            }
            PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
            if (targetplayerData.getFactionGrade() >= 5) {
                if (rang < 5) {
                    TeamSpeak.reloadPlayer(targetplayerData.getUuid());
                }
            }
            targetplayerData.setFactionGrade(rang);
            if (targetplayerData.getFactionGrade() >= 5) {
                TeamSpeak.reloadPlayer(targetplayer.getUniqueId());
            }
            target.sendMessage("§8[§cAdmin§8]§c " + playerData.getRang() + " " + player.getName() + " hat dir Rang " + rang + " gegeben! (Administrativ)");
        }
        return false;
    }
}
