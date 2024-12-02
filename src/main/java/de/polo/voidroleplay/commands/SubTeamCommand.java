package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.game.faction.staat.SubTeam;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SubTeamCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public SubTeamCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;

        Main.registerCommand("subteam", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) commandSender;
        if (!playerManager.isInStaatsFrak(player)) {
            player.sendMessage(Prefix.error_nopermission);
            return false;
        }
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFactionGrade() < 5) {
            player.sendMessage(Prefix.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /subteam [list/add/remove/invite/uninvite] [Name/Spieler]");
            return false;
        }
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (args[0].equalsIgnoreCase("list")) {
            player.sendMessage("§7   ===§8[§9Sub-Teams§8]§7===");
            for (SubTeam subTeam : factionManager.getSubTeams(factionData.getId())) {
                player.sendMessage("§8 - §3" + subTeam.getName());
            }
            return false;
        } else if (args.length < 2) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /subteam [list/add/remove/invite/uninvite] [Name/Spieler]");
            return false;
        }
        StringBuilder value = new StringBuilder(args[1]);
        for (int i = 2; i < args.length; i++) {
            value.append(" ").append(args[i]);
        }
        if (args[0].equalsIgnoreCase("add")) {
            for (SubTeam team : factionManager.getSubTeams(factionData.getId())) {
                if (team.getName().equalsIgnoreCase(value.toString())) {
                    player.sendMessage(Prefix.ERROR + "Es gibt bereits ein Sub-Team mit diesem Namen.");
                    return false;
                }
            }
            factionManager.sendCustomLeaderMessageToFactions("§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§9 " + player.getName() + " hat das Sub-Team \"" + value + "\" erstellt.", factionData.getName());
            SubTeam subTeam = new SubTeam(factionData.getId(), value.toString());
            factionManager.createSubTeam(subTeam);
        } else if (args[0].equalsIgnoreCase("remove")) {
            for (SubTeam team : factionManager.getSubTeams(factionData.getId())) {
                if (team.getName().equalsIgnoreCase(value.toString())) {
                    player.sendMessage(Prefix.ERROR + "Es gibt bereits ein Sub-Team mit diesem Namen.");
                    factionManager.sendCustomLeaderMessageToFactions("§8[§" + factionData.getPrimaryColor() + factionData.getName() + "§8]§9 " + player.getName() + " hat das Sub-Team \"" + value + "\" gelöscht.", factionData.getName());
                    factionManager.deleteSubTeam(team);
                    return false;
                }
            }
            player.sendMessage(Prefix.ERROR + "Die Gruppe wurde nicht gefunden.");
        } else if (args[0].equalsIgnoreCase("invite")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(Prefix.ERROR + "Spieler wurde nichtg gefunden.");
                return false;
            }
            PlayerData targetData = playerManager.getPlayerData(target);
            if (targetData.getFaction() == null) {
                player.sendMessage(Prefix.ERROR + "Der Spieler ist nicht in deiner Fraktion.");
                return false;
            }
            if (!targetData.getFaction().equals(playerData.getFaction())) {
                player.sendMessage(Prefix.ERROR + "Der Spieler ist nicht in deiner Fraktion.");
                return false;
            }
            if (args.length < 3) {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /subteam [invite] [Spieler] [Team]");
                return false;
            }
            value = new StringBuilder(args[2]);
            for (int i = 3; i < args.length; i++) {
                value.append(" ").append(args[i]);
            }
            for (SubTeam team : factionManager.getSubTeams(factionData.getId())) {
                if (team.getName().equalsIgnoreCase(value.toString())) {
                    player.sendMessage(Prefix.MAIN + "Du hast " + target.getName() + " in das Sub-Team hinzugefügt.");
                    targetData.setSubTeam(team);
                    targetData.getSubTeam().sendMessage("§8[§3" + playerData.getSubTeam().getName() + "§8]§7 " + player.getName() + " " + " hat " + target.getName() + " in das Team eingeladen!");
                    targetData.save();
                    return false;
                }
            }
            player.sendMessage(Prefix.ERROR + "Das Team wurde nicht gefunden!");
        } else if (args[0].equalsIgnoreCase("uninvite")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(Prefix.ERROR + "Spieler wurde nichtg gefunden.");
                return false;
            }
            PlayerData targetData = playerManager.getPlayerData(target);
            if (targetData.getFaction() == null) {
                player.sendMessage(Prefix.ERROR + "Der Spieler ist nicht in deiner Fraktion.");
                return false;
            }
            if (!targetData.getFaction().equals(playerData.getFaction())) {
                player.sendMessage(Prefix.ERROR + "Der Spieler ist nicht in deiner Fraktion.");
                return false;
            }
            player.sendMessage(Prefix.MAIN + "Du hast " + target.getName() + " aus dem Sub-Team geworfen!");
            targetData.setSubTeam(null);
            targetData.getSubTeam().sendMessage("§8[§3" + playerData.getSubTeam().getName() + "§8]§7 " + player.getName() + " " + " hat " + target.getName() + " aus dem Team geworfen!");
            targetData.save();
            player.sendMessage(Prefix.ERROR + "Das Team wurde nicht gefunden!");
        } else {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /subteam [list/add/remove/invite/uninvite] [Name/Spieler]");
        }
        return false;
    }
}
