package de.polo.core.utils;

import de.polo.core.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

@Event
public class AntiCheat implements Listener, PluginMessageListener {

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (channel.equalsIgnoreCase("WDL|INIT")) {
            player.kickPlayer("§cDu nutzt nicht zulässig Client-Modifikationen.");
        }
    }
}
