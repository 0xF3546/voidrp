package de.polo.core.utils.player;

import de.polo.core.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Progress {
    public static boolean startWithCallback(Player player, int time) {
        return false;
    }

    public static void start(Player player, int time) {
        final int[] barsFilled = {0};
        final int ticksPerBar = time * 20 / 6;
        final int totalTicks = 6 * ticksPerBar;
        final int timePerBar = time / 6;
        final String actionBar = "§8[§a######§7######§8]";

        new BukkitRunnable() {
            int currentTick = 0;

            @Override
            public void run() {
                currentTick++;
                if (currentTick % ticksPerBar == 0) {
                    barsFilled[0]++;
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < barsFilled[0]; i++) {
                        sb.append("§a#");
                    }
                    for (int i = barsFilled[0]; i < 6; i++) {
                        sb.append("§7#");
                    }
                    String actionBarString = sb.toString();
                    // send actionbar to player
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBarString));
                }
                if (currentTick >= totalTicks) {
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

    public static void startWithTitle(Player player, int time) {
        final int[] barsFilled = {0};
        final int ticksPerBar = time * 20 / 6;
        final int totalTicks = 6 * ticksPerBar;
        final int timePerBar = time / 6;
        final String actionBar = "§8[§a▎▎▎▎▎▎§7▎▎▎▎▎▎§8]";

        new BukkitRunnable() {
            int currentTick = 0;

            @Override
            public void run() {
                currentTick++;
                if (currentTick % ticksPerBar == 0) {
                    barsFilled[0]++;
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < barsFilled[0]; i++) {
                        sb.append("§a▎");
                    }
                    for (int i = barsFilled[0]; i < 6; i++) {
                        sb.append("§7▎");
                    }
                    String actionBarString = sb.toString();
                    // send actionbar to player
                    player.sendTitle(actionBarString, "", 0, 40, 0);
                }
                if (currentTick >= totalTicks) {
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }


}
