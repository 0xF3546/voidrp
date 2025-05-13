package de.polo.core.admin.commands;

import de.polo.api.player.VoidPlayer;
import de.polo.core.admin.utils.ServerStats;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.GlobalStats;
import de.polo.core.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;

@CommandBase.CommandMeta(
        name = "serverstats",
        permissionLevel = 60
)
public class ServerStatsCommand extends CommandBase {
    public ServerStatsCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        LocalDateTime startTime = ServerStats.getStartTime();
        LocalDateTime now = Utils.getTime();
        Duration uptime = Duration.between(startTime, now);

        long hours = uptime.toHours();
        long minutes = uptime.toMinutes() % 60;
        long seconds = uptime.getSeconds() % 60;

        player.sendMessage("§7   ===§8[§cServer Stats§8]§7===");
        player.sendMessage("§8 - §7Uptime§8: §a" + Utils.localDateTimeToReadableString(startTime)
                + " §8(§7" + hours + "h " + minutes + "m " + seconds + "s§8)");
        player.sendMessage("§8 - §7Spieler Peak§8: §a" + GlobalStats.getValue("peakPlayers") + " §8(§7" + ServerStats.getPeakPlayers() + " heute§8)");
    }
}
