package de.polo.metropiacity.discord;

import de.polo.metropiacity.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;

public class discord {
    static JDA bot = null;
    public static void runBot() throws InterruptedException {
        bot = JDABuilder.createDefault("MTA2NjExOTE4MTk4MTk3ODY4NA.GYIgqN.1JVKQCshvBoI09PM-C-Z4qufCZpw28SRdH9XYM")
                .addEventListeners(new ReadyListener())
                .setActivity(Activity.watching("auf Void Roleplay"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MESSAGES)
                .build();
        bot.setAutoReconnect(true);
        bot.awaitReady();
        bot.getPresence().setStatus(OnlineStatus.IDLE);
        do {
            if (Main.getInstance().isOnline) {
                bot.getPresence().setActivity(Activity.watching("auf Void Roleplay [" + (long) Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers() + "]"));
                Thread.sleep(1300);
                bot.getPresence().setActivity(Activity.watching("auf Void Roleplay [" + (long) Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers() + "]"));
            }
        } while (true);
    }
    public static void sendLog(String log) {
        if (bot != null) bot.getPrivateChannelById("1106616129704689744").sendMessage(log);
    }

    public static void sendJoinLeaveMessage(String log) {
        if (bot != null) bot.getPrivateChannelById("1106616374895317104").sendMessage(log);
    }
}
