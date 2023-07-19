package de.polo.metropiacity.Listener;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.commands.Aduty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerVoteListener implements Listener {
        @EventHandler
        public void onVote(VotifierEvent event) {
            Vote vote = event.getVote();
            Player player = Bukkit.getPlayer(vote.getUsername());
            assert player != null;
            Aduty.send_message(vote.getUsername() + " hat über " + vote.getServiceName() + " gevotet.");
            if(player.isOnline()) {
                player.sendMessage(Main.prefix + "§6§lDanke§7 für deinen Vote!");
                PlayerManager.addExp(player, Main.random(30, 50));
            }
        }
}
