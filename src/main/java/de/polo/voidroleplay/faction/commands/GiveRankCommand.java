package de.polo.voidroleplay.faction.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.faction.entity.Faction;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.TeamSpeak;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        if (!playerData.isLeader()) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /giverank [Spieler] [Rang]");
            return false;
        }
        OfflinePlayer targetplayer = Utils.getOfflinePlayer(args[0]);
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
        PlayerData targetData = factionManager.getFactionOfPlayer(targetplayer.getUniqueId());
        try {
            if (targetData.getFaction() == null || !targetData.getFaction().equals(playerData.getFaction())) {
                player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in deiner Fraktion.");
                return false;
            }
            if (targetData.getFactionGrade() >= playerData.getFactionGrade()) {
                player.sendMessage(Prefix.ERROR_NOPERMISSION);
                return false;
            }
            if (targetData.getFactionJoin().plusWeeks(rang).isAfter(Utils.getTime())) {
                player.sendMessage(Prefix.ERROR + "Der Spieler ist noch nicht lang genug in der Fraktion.");
                return false;
            }
        } catch (Exception ex) {
            player.sendMessage(Prefix.ERROR + "Fehler beim setzen der Ränge, bitte warte bis der Server neugestartet wurde.");
            return false;
        }
        Faction factionData = factionManager.getFactionData(playerData.getFaction());
        player.sendMessage("§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§7 Du hast " + targetplayer.getName() + " Rang " + rang + " gegeben!");
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET faction_grade = ? WHERE uuid = ?", rang, targetplayer.getUniqueId().toString());
        if (targetplayer.isOnline()) {
            Player target = Bukkit.getPlayer(args[0]);
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
            target.sendMessage("§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§7 " + factionManager.getPlayerFactionRankName(player) + " " + player.getName() + " hat dir Rang " + rang + " gegeben!");
        }
        return false;
    }
}
