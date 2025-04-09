package de.polo.core.admin.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.admin.services.impl.AdminManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static de.polo.core.Main.adminService;

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
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() >= 100) {
            if (args.length == 2) {
                OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
                if (offlinePlayer == null) {
                    player.sendMessage(Prefix.ERROR + "Spieler wurde nicht gefunden.");
                    return false;
                }
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
                Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET rankDuration = null WHERE uuid = ?", offlinePlayer.getUniqueId().toString());
                playerData.setRankDuration(null);
                playerManager.setRang(offlinePlayer.getUniqueId(), rank);
                adminService.send_message(player.getName() + " hat " + offlinePlayer.getName() + " den Rang " + rank + " gegeben.", Color.RED);
            } else {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /setgroup [Spieler] [Rang]");
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }
}
