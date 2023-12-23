package de.polo.metropiacity.utils;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.playerUtils.ChatUtils;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class Server {
    //static de.polo.metropiacity.utils.TeamSpeak teamSpeak = null;
    public Server() {
        init();
    }
    private void init() {
        //teamSpeak = new de.polo.metropiacity.utils.TeamSpeak();
    }
    public interface Utils {
        static void kissPlayer(Player player, Player targetplayer) {
            if (player.getLocation().distance(targetplayer.getLocation()) < 5) {
                ChatUtils.sendMeMessageAtPlayer(player, "§o" + player.getName() + " gibt " + targetplayer.getName() + " einen Kuss.");
                player.spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 1);
                targetplayer.spawnParticle(Particle.HEART, targetplayer.getLocation().add(0, 2, 0), 1);
            } else {
                player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner nähe.");
            }
        }
    }
}
