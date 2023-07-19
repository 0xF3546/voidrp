package de.polo.metropiacity.Utils;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.PlayerUtils.ChatUtils;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class Server {
    public interface TeamSpeak {
    }
    public interface Faction {

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
    public interface Economy {
        interface farming {

        }
        interface jobs {

        }
        interface user {
            static void addMoney(Player player, int amount) {
            }
            static void removeMoney(Player player, int amount) {

            }
        }
    }
}
