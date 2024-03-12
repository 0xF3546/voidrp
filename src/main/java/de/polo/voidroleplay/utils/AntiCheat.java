package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class AntiCheat implements Listener, PluginMessageListener {
    public AntiCheat() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (channel.equalsIgnoreCase("WDL|INIT")) {
            player.kickPlayer("§cDu nutzt nicht zulässig Client-Modifikationen.");
        }
    }
}
