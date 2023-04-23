package de.polo.void_roleplay.Listener;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class playerVoteListener implements Listener {
        @EventHandler
        public void onVote(final VotifierEvent event) {
            Vote vote = event.getVote();
            Player player = Bukkit.getPlayer(vote.getUsername());
            System.out.println(event.getVote().getUsername() + " hat gevotet.");
            assert player != null;
            if(player.isOnline()) {
                Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.toString() + ChatColor.DARK_GREEN + " has voted for the server!");
                player.sendMessage(Main.prefix + "§6§lDanke§7 für deinen Vote!");
                PlayerManager.addExp(player, Main.random(50, 100));
            }
        }
}
