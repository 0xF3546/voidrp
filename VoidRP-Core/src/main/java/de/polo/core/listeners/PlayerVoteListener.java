package de.polo.core.listeners;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.admin.services.impl.AdminManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.UUID;

import static de.polo.core.Main.*;

public class PlayerVoteListener implements Listener {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final HashMap<UUID, Integer> votes = new HashMap<>();

    public PlayerVoteListener(PlayerManager playerManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        Main.registerListener(this);
    }

    @SneakyThrows
    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();
        Player player = Bukkit.getPlayer(vote.getUsername());
        assert player != null;
        adminService.send_message(vote.getUsername() + " hat über " + vote.getServiceName() + " gevotet.", Color.GRAY);
        if (player.isOnline()) {
            PlayerData playerData = playerService.getPlayerData(player);
            player.sendMessage(Prefix.MAIN + "§6§lDanke§7 für deinen Vote!");
            playerManager.addExp(player, Utils.random(30, 50));
            votes.putIfAbsent(player.getUniqueId(), 1);
            if (votes.get(player.getUniqueId()) > 2) {
                return;
            }
            votes.replace(player.getUniqueId(), votes.get(player.getUniqueId()) + 1);
            playerManager.addCoins(player, Utils.random(10, 13));
            playerData.setVotes(playerData.getVotes() + 1);
            database.updateAsync("UPDATE players SET votes = ? WHERE uuid = ?", playerData.getVotes(), player.getUniqueId().toString());

            LogVote(player.getUniqueId().toString(), vote.getServiceName());
        }
    }

    @SneakyThrows
    private void LogVote(String uuid, String page) {
        database.insertAsync("INSERT INTO vote_log (uuid, page) VALUES (?, ?)", uuid, page);
    }
}
