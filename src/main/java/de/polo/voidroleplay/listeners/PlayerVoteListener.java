package de.polo.voidroleplay.listeners;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.AdminManager;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerVoteListener implements Listener {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;

    public PlayerVoteListener(PlayerManager playerManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();
        Player player = Bukkit.getPlayer(vote.getUsername());
        assert player != null;
        adminManager.send_message(vote.getUsername() + " hat über " + vote.getServiceName() + " gevotet.", ChatColor.GRAY);
        if (player.isOnline()) {
            player.sendMessage(Main.prefix + "§6§lDanke§7 für deinen Vote!");
            playerManager.addExp(player, Main.random(30, 50));
            playerManager.addCoins(player, Main.random(10, 13));
        }
    }
}
