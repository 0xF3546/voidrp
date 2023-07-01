package de.polo.metropiacity.Utils;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.PlayerUtils.ChatUtils;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Calendar;

public class Utils {
    static int minutes = 1;
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
    }

    public static void sendBossBar(Player player, String text) {
    }

    public static void kissPlayer(Player player, Player targetplayer) {
        if (player.getLocation().distance(targetplayer.getLocation()) < 5) {
            ChatUtils.sendMeMessageAtPlayer(player, "§o" + player.getName() + " gibt " + targetplayer.getName() + " einen Kuss.");
            player.spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 1);
            targetplayer.spawnParticle(Particle.HEART, targetplayer.getLocation().add(0, 2, 0), 1);
        } else {
            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner nähe.");
        }
    }

    public static int getCurrentMinute() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }
    public static int getCurrentHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }
}
