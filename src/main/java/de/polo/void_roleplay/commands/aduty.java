package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.PlayerUtils.Scoreboard;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class aduty implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (PlayerManager.onPlayer.get(player.getUniqueId().toString())) {
            if (playerData.isAduty()) {
                playerData.setAduty(false);
                player.sendMessage(Main.admin_prefix + "Du hast den Admindienst §cverlassen§7.");
                (player).setFlying(false);
                (player).setAllowFlight(false);
                player.getPlayer().setPlayerListName("§8[§7Team§8]§7 " + player.getName());
                player.getPlayer().setDisplayName("§8[§7Team§8]§7 " + player.getName());
                playerData.getScoreboard().killScoreboard();
            } else {
                playerData.setAduty(true);
                player.sendMessage(Main.admin_prefix + "Du hast den Admindienst §abetreten§7.");
                (player).setAllowFlight(true);
                player.getPlayer().setPlayerListName("§8[§cTeam§8]§c " + player.getName());
                player.getPlayer().setDisplayName("§8[§cTeam§8]§c " + player.getName());
                playerData.getScoreboard().createAdminScoreboard();
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }

    public static void send_message(String msg) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = PlayerManager.playerDataMap.get(player1.getUniqueId().toString());
            if (playerData.isAduty()) {
                player1.sendMessage(Main.admin_info + msg);
            }
        }
    }
}
