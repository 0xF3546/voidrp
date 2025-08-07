package de.polo.voidroleplay.admin.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.DBPlayerData;
import de.polo.voidroleplay.faction.entity.FactionPlayerData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.utils.Prefix;
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
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /agiverank [Spieler] [Rang]");
            return false;
        }
        OfflinePlayer targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Prefix.ERROR + args[0] + " wurde nicht gefunden.");
            return false;
        }
        int rang;
        try {
            rang = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Prefix.ERROR + "Der Rang muss eine Zahl sein!");
            return false;
        }
        if (0 > rang || rang > 8) {
            player.sendMessage(Prefix.ERROR + "Der Rang muss von 0-8 sein!");
            return false;
        }
        DBPlayerData dbPlayerData = ServerManager.dbPlayerDataMap.get(targetplayer.getUniqueId().toString());
        FactionPlayerData factionPlayerData = ServerManager.factionPlayerDataMap.get(targetplayer.getUniqueId().toString());
        player.sendMessage("§8[§cAdmin§8]§7 Du hast " + targetplayer.getName() + " Rang " + rang + " gegeben!");
        if (factionPlayerData != null) {
            factionPlayerData.setFaction_grade(rang);
        }
        if (dbPlayerData != null) {
            dbPlayerData.setFaction_grade(rang);
        }
        try {
            Statement statement = Main.getInstance().getCoreDatabase().getStatement();
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
            if (targetplayerData.isLeader()) {
                if (rang < 5) {
                    TeamSpeak.reloadPlayer(targetplayerData.getUuid());
                }
            }
            targetplayerData.setFactionGrade(rang);
            if (targetplayerData.isLeader()) {
                TeamSpeak.reloadPlayer(targetplayer.getUniqueId());
            }
            target.sendMessage("§8[§cAdmin§8]§c " + playerData.getRang() + " " + player.getName() + " hat dir Rang " + rang + " gegeben! (Administrativ)");
        }
        return false;
    }
}
