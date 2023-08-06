package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ADutyCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() < 60) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length == 0) {
            if (playerData.isAduty()) {
                playerData.setAduty(false);
                ADutyCommand.send_message(player.getName() + " hat den Admindienst verlassen.", ChatColor.RED);
                player.sendMessage(Main.admin_prefix + "Du hast den Admindienst §cverlassen§7.");
                (player).setFlying(false);
                (player).setAllowFlight(false);
                player.getPlayer().setPlayerListName("§8[§7Team§8]§7 " + player.getName());
                player.getPlayer().setDisplayName("§8[§7Team§8]§7 " + player.getName());
                playerData.getScoreboard().killScoreboard();
                if (playerData.isDuty()) {
                    FactionManager.setDuty(player, true);
                }
                if (playerData.getVariable("isSpec") != null) {
                    SpecCommand.leaveSpec(player);
                }
            } else {
                ADutyCommand.send_message(player.getName() + " hat den Admindienst betreten.", ChatColor.RED);
                playerData.setAduty(true);
                player.sendMessage(Main.admin_prefix + "Du hast den Admindienst §abetreten§7.");
                (player).setAllowFlight(true);
                player.getPlayer().setPlayerListName("§8[§cTeam§8]§c " + player.getName());
                player.getPlayer().setDisplayName("§8[§cTeam§8]§c " + player.getName());
                playerData.getScoreboard().createAdminScoreboard();
            }
        }
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "-v":
                    if (playerData.getVariable("isVanish") == null) {
                        player.sendMessage(Main.admin_prefix + "Du bist nun im Vanish.");
                        playerData.setVariable("isVanish", "D:");
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            players.hidePlayer(Main.getInstance(), player);
                        }
                        send_message(player.getName() + " hat den Vanish betreten.", null);
                    } else {
                        player.sendMessage(Main.admin_prefix + "Du bist nun nicht mehr im Vanish.");
                        playerData.setVariable("isVanish", null);
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            players.showPlayer(Main.getInstance(), player);
                        }
                        send_message(player.getName() + " hat den Vanish verlassen.", null);
                    }
                    break;
            }
        }
        return false;
    }

    public static void send_message(String msg, ChatColor color) {
        if (color == null) {
            color = ChatColor.AQUA;
        }
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = PlayerManager.playerDataMap.get(player1.getUniqueId().toString());
            if (playerData.isAduty()) {
                player1.sendMessage("§8[§c§l!§8] " + color + msg);
            }
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("-v");

            return suggestions;
        }
        return null;
    }
}
