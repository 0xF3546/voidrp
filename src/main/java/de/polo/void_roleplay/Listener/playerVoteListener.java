package de.polo.void_roleplay.Listener;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.commands.aduty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class playerVoteListener implements Listener {
        @EventHandler
        public void onVote(VotifierEvent event) {
            Vote vote = event.getVote();
            Player player = Bukkit.getPlayer(vote.getUsername());
            assert player != null;
            aduty.send_message(vote.getUsername() + " hat über " + vote.getServiceName() + " gevotet.");
            if(player.isOnline()) {
                player.sendMessage(Main.prefix + "§6§lDanke§7 für deinen Vote!");
                PlayerManager.addExp(player, Main.random(30, 50));
            }
        }
}
