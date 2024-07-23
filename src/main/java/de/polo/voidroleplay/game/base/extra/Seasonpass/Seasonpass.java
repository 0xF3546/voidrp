package de.polo.voidroleplay.game.base.extra.Seasonpass;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.PlayerManager;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Seasonpass implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private List<Quest> quests = new ArrayList<>();
    private List<Reward> rewards = new ArrayList<>();
    public Seasonpass(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("seasonpass", this);
        loadAll();
    }

    @SneakyThrows
    public void loadAll() {
        quests.clear();
        rewards.clear();
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM seasonpass_quests");
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            Quest quest = new Quest(result.getInt("id"), result.getInt("points"), result.getString("name"), result.getString("description"));
            quest.setRewardId(result.getInt("rewardId"));
            quests.add(quest);
        }

        statement = connection.prepareStatement("SELECT * FROM seasonpass_rewards");
        result = statement.executeQuery();
        while (result.next()) {
            Reward reward = new Reward(result.getInt("id"), result.getString("type"), result.getInt("amount"));
            reward.setInfo(result.getString("info"));
            rewards.add(reward);
        }
        statement.close();
        connection.close();
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return false;
    }
}
