package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.WantedReason;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WantedInfoCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;
    public WantedInfoCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;

        Main.registerCommand("wantedinfo", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) commandSender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.isExecutiveFaction()) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /wantedinfo [Spieler]");
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Prefix.ERROR + "Der Spieler wurde nicht gefunden.");
            return false;
        }
        PlayerData targetData = playerManager.getPlayerData(target);
        if (targetData.getWanted() == null) {
            player.sendMessage(Prefix.ERROR + "Der Spieler wird nicht gesucht.");
            return false;
        }
        WantedReason wantedReason = utils.getStaatUtil().getWantedReason(targetData.getWanted().getWantedId());
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetData.getWanted().getIssuer());
        player.sendMessage("§9HQ: Fahndung " + target.getName() + ": " + wantedReason.getReason() + " (" + wantedReason.getWanted() + " WPS)");
        player.sendMessage("§9HQ: Beamter: " + offlinePlayer.getName() + ", " + Utils.localDateTimeToReadableString(targetData.getWanted().getIssued()));
        return false;
    }
}
