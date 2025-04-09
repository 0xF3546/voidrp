package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.handler.CommandBase;
import de.polo.core.handler.TabCompletion;
import de.polo.core.manager.ServerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RankData;
import de.polo.core.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static de.polo.core.Main.scoreboardAPI;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "aduty",
        usage = "/aduty [-v]"
)
public class AdutyCommand extends CommandBase implements TabCompleter {
    public AdutyCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (playerData.getPermlevel() < 60) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return;
        }
        RankData rankData = ServerManager.rankDataMap.get(playerData.getRang());
        AdminService adminService = VoidAPI.getService(AdminService.class);
        if (args.length == 0) {
            if (player.isAduty()) {

                player.setAduty(false);
                adminService.send_message(player.getName() + " hat den Admindienst verlassen.", Color.RED);
                player.sendMessage(Prefix.ADMIN + "Du hast den Admindienst §cverlassen§7.");
                player.getPlayer().setFlying(false);
                player.getPlayer().setAllowFlight(false);
                scoreboardAPI.removeScoreboard(player.getPlayer(), "admin");
                player.getPlayer().setCollidable(true);
            } else {

                adminService.send_message(player.getName() + " hat den Admindienst betreten.", Color.RED);
                player.setAduty(true);
                player.sendMessage(Prefix.ADMIN + "Du hast den Admindienst §abetreten§7.");
                player.getPlayer().setAllowFlight(true);

                scoreboardAPI.createScoreboard(player.getPlayer(), "admin", "§cAdmindienst", () -> {
                    // Set initial scores
                    scoreboardAPI.setScore(player.getPlayer(), "admin", "§6Tickets offen§8:", Main.getInstance().supportManager.getTickets().size());
                    Runtime r = Runtime.getRuntime();
                    scoreboardAPI.setScore(player.getPlayer(), "admin", "§6Auslastung§8:", (int) (r.totalMemory() - r.freeMemory()) / 1048576);
                    scoreboardAPI.setScore(player.getPlayer(), "admin", "§6Spieler Online§8:", Bukkit.getOnlinePlayers().size());
                });

                startMemoryUsageUpdater(player.getPlayer()); // Startet den Memory Usage Updater

                playerData.setScoreboard("admin", scoreboardAPI.getScoreboard(player.getPlayer(), "admin"));
                player.getPlayer().setCollidable(false);
            }
        }
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "-v":
                    if (playerData.getVariable("isVanish") == null) {
                        player.sendMessage(Prefix.ADMIN + "Du bist nun im Vanish.");
                        playerData.setVariable("isVanish", "D:");
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            players.hidePlayer(Main.getInstance(), player.getPlayer());
                        }
                        adminService.send_message(player.getName() + " hat den Vanish betreten.", null);
                    } else {
                        player.sendMessage(Prefix.ADMIN + "Du bist nun nicht mehr im Vanish.");
                        playerData.setVariable("isVanish", null);
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            players.showPlayer(Main.getInstance(), player.getPlayer());
                        }
                        adminService.send_message(player.getName() + " hat den Vanish verlassen.", null);
                    }
                    break;
            }
        }
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

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, "-v")
                .build();
    }
}
