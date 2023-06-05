package de.polo.metropiacity.Utils;

import org.bukkit.entity.Player;

public class Utils {
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
    }
}
