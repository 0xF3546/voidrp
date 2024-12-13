package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetTeamCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;

    public SetTeamCommand(PlayerManager playerManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        Main.registerCommand("setgroup", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (player.hasPermission("operator")) {
            if (args.length == 2) {
                OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
                if (offlinePlayer == null) {
                    player.sendMessage(Prefix.ERROR + "Spieler wurde nicht gefunden.");
                    return false;
                }
                PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
                String rank = args[1];
                if (ServerManager.rankDataMap.get(rank) == null) {
                    player.sendMessage(Prefix.ERROR + "Rang nicht gefunden.");
                    return false;
                }
                if (offlinePlayer.isOnline()) {
                    Player targetplayer = Bukkit.getPlayer(offlinePlayer.getUniqueId());
                    targetplayer.sendMessage(Prefix.ADMIN + "Du bist nun §c" + rank + "§7!");
                    targetplayer.sendMessage("§b   Info§8:§f Da du nun Teammitglied bist, hast du deine Spielerränge verloren.");
                }
                player.sendMessage(Prefix.ADMIN + offlinePlayer.getName() + " ist nun §c" + rank + "§7.");
                Main.getInstance().getMySQL().updateAsync("UPDATE players SET rankDuration = null WHERE uuid = ?", offlinePlayer.getUniqueId().toString());
                playerData.setRankDuration(null);
                playerManager.setRang(offlinePlayer.getUniqueId(), rank);
                adminManager.send_message(player.getName() + " hat " + offlinePlayer.getName() + " den Rang " + rank + " gegeben.", ChatColor.DARK_RED);
            } else {
                player.sendMessage(Prefix.ADMIN_ERROR + "Syntax-Fehler: /setgroup [Spieler] [Rang]");
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }
}
