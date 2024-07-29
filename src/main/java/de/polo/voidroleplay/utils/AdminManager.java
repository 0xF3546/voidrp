package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.RankData;
import de.polo.voidroleplay.utils.playerUtils.Scoreboard;
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

public class AdminManager implements CommandExecutor, TabCompleter {

    private final PlayerManager playerManager;
    public AdminManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 60) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        RankData rankData = ServerManager.rankDataMap.get(playerData.getRang());
        if (args.length == 0) {
            if (playerData.isAduty()) {
                playerData.setAduty(false);
                send_message(player.getName() + " hat den Admindienst verlassen.", ChatColor.RED);
                player.sendMessage(Main.admin_prefix + "Du hast den Admindienst §cverlassen§7.");
                (player).setFlying(false);
                (player).setAllowFlight(false);
                playerData.getScoreboard("admin").killScoreboard();
                if (playerData.getVariable("isSpec") != null) {
                    Main.getInstance().commands.specCommand.leaveSpec(player);
                }
                //Utils.Display.adminMode(player, false);
                player.setCollidable(true);
            } else {
                send_message(player.getName() + " hat den Admindienst betreten.", ChatColor.RED);
                playerData.setAduty(true);
                player.sendMessage(Main.admin_prefix + "Du hast den Admindienst §abetreten§7.");
                (player).setAllowFlight(true);
                Scoreboard adminScoreboard = new Scoreboard(player);
                adminScoreboard.createAdminScoreboard();
                playerData.setScoreboard("admin", adminScoreboard);
                player.setCollidable(false);
                //Utils.Display.adminMode(player, true);
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

    public void send_message(String msg, ChatColor color) {
        if (color == null) {
            color = ChatColor.AQUA;
        }
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = playerManager.getPlayerData(player1.getUniqueId());
            if (playerData.isAduty()) {
                player1.sendMessage("§8[§c§l!§8] " + color + msg);
            }
        }
    }

    public void sendGuideMessage(String msg, ChatColor color) {
        if (color == null) {
            color = ChatColor.AQUA;
        }
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = playerManager.getPlayerData(player1.getUniqueId());
            if (playerData.getPermlevel() >= 40) {
                player1.sendMessage("§8[§eGuide§8] " + color + msg);
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
