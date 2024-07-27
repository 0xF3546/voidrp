package de.polo.voidroleplay.listeners;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.AdminManager;
import de.polo.voidroleplay.utils.PlayerManager;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.PreparedStatement;

public class PlayerVoteListener implements Listener {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;

    public PlayerVoteListener(PlayerManager playerManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @SneakyThrows
    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();
        Player player = Bukkit.getPlayer(vote.getUsername());
        assert player != null;
        adminManager.send_message(vote.getUsername() + " hat über " + vote.getServiceName() + " gevotet.", ChatColor.GRAY);
        if (player.isOnline()) {
            PlayerData playerData = playerManager.getPlayerData(player);
            player.sendMessage(Main.prefix + "§6§lDanke§7 für deinen Vote!");
            playerManager.addExp(player, Main.random(30, 50));
            playerManager.addCoins(player, Main.random(10, 13));
            playerData.setVotes(playerData.getVotes() + 1);
            PreparedStatement preparedStatement = Main.getInstance().mySQL.getConnection().prepareStatement("UPDATE players SET votes = ? WHERE uuid = ?");
            preparedStatement.setInt(1, playerData.getVotes());
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.executeUpdate();
            preparedStatement.close();

            LogVote(player.getUniqueId().toString(), vote.getServiceName());
        }
    }

    @SneakyThrows
    private void LogVote(String uuid, String page) {
        PreparedStatement preparedStatement = Main.getInstance().mySQL.getConnection().prepareStatement("INSERT INTO vote_log (uuid, page) VALUES (?, ?)");
        preparedStatement.setString(1, uuid);
        preparedStatement.setString(2, page);
        preparedStatement.execute();
        preparedStatement.close();
    }
}
