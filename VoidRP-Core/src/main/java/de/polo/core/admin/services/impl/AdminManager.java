package de.polo.core.admin.services.impl;

import de.polo.core.Main;
import de.polo.core.handler.TabCompletion;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RankData;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.player.ScoreboardAPI;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class AdminManager implements CommandExecutor, TabCompleter {

    private final PlayerManager playerManager;
    private final ScoreboardAPI scoreboardAPI;

    public AdminManager(PlayerManager playerManager, ScoreboardAPI scoreboardAPI) {
        this.playerManager = playerManager;
        this.scoreboardAPI = scoreboardAPI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 60) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        RankData rankData = ServerManager.rankDataMap.get(playerData.getRang());
        if (args.length == 0) {
            if (playerData.isAduty()) {

                playerData.setAduty(false);
                send_message(player.getName() + " hat den Admindienst verlassen.", ChatColor.RED);
                player.sendMessage(Prefix.ADMIN + "Du hast den Admindienst §cverlassen§7.");
                player.setFlying(false);
                player.setAllowFlight(false);
                scoreboardAPI.removeScoreboard(player, "admin");
                player.setCollidable(true);
            } else {

                send_message(player.getName() + " hat den Admindienst betreten.", ChatColor.RED);
                playerData.setAduty(true);
                player.sendMessage(Prefix.ADMIN + "Du hast den Admindienst §abetreten§7.");
                player.setAllowFlight(true);

                scoreboardAPI.createScoreboard(player, "admin", "§cAdmindienst", () -> {
                    // Set initial scores
                    scoreboardAPI.setScore(player, "admin", "§6Tickets offen§8:", Main.getInstance().supportManager.getTickets().size());
                    Runtime r = Runtime.getRuntime();
                    scoreboardAPI.setScore(player, "admin", "§6Auslastung§8:", (int) (r.totalMemory() - r.freeMemory()) / 1048576);
                    scoreboardAPI.setScore(player, "admin", "§6Spieler Online§8:", Bukkit.getOnlinePlayers().size());
                });

                startMemoryUsageUpdater(player); // Startet den Memory Usage Updater

                playerData.setScoreboard("admin", scoreboardAPI.getScoreboard(player, "admin"));
                player.setCollidable(false);
            }
        }
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "-v":
                    if (playerData.getVariable("isVanish") == null) {
                        player.sendMessage(Prefix.ADMIN + "Du bist nun im Vanish.");
                        playerData.setVariable("isVanish", "D:");
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            players.hidePlayer(Main.getInstance(), player);
                        }
                        send_message(player.getName() + " hat den Vanish betreten.", null);
                    } else {
                        player.sendMessage(Prefix.ADMIN + "Du bist nun nicht mehr im Vanish.");
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

    private void updateMemoryUsage(Player player) {
        Runtime r = Runtime.getRuntime();
        long usedMemory = (r.totalMemory() - r.freeMemory()) / 1024 / 1024; // in MB
        long maxMemory = r.maxMemory() / 1024 / 1024; // in MB

        scoreboardAPI.setScore(player, "admin", "§6Auslastung§8:", (int) usedMemory);
    }

    public void startMemoryUsageUpdater(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    updateMemoryUsage(player);
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L * 60); // Aktualisiert jede Minute
    }

    public void send_message(String msg, ChatColor color) {
        if (color == null) {
            color = ChatColor.AQUA;
        }
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = playerManager.getPlayerData(player1.getUniqueId());
            if (playerData.isAduty() || playerData.isSendAdminMessages()) {
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

    @SneakyThrows
    public void insertNote(String punisher, String target, String note) {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO notes (uuid, target, note) VALUES (?, ?, ?)");
        statement.setString(1, punisher);
        statement.setString(2, target);
        statement.setString(3, note);
        statement.execute();
        statement.close();
        connection.close();
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, "-v")
                .build();
    }
}