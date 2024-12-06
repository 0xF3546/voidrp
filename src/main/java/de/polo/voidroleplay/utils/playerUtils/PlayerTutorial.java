package de.polo.voidroleplay.utils.playerUtils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerTutorial {
    private final Player player;
    private final PlayerData playerData;
    private int stage = 1;

    public PlayerTutorial(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public PlayerTutorial(Player player, PlayerData playerData, int stage) {
        this.player = player;
        this.playerData = playerData;
        this.stage = stage;
    }

    public static PlayerTutorial getPlayerTutorial(PlayerData playerData) {
        if (playerData.getVariable("tutorial") == null) return null;
        return playerData.getVariable("tutorial");
    }

    public int getStage() {
        return stage;
    }

    @SneakyThrows
    public void setStage(int stage) {
        this.stage = stage;
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE players SET tutorial = ? WHERE uuid = ?");
        statement.setInt(1, stage);
        statement.setString(2, player.getUniqueId().toString());
        statement.execute();

        statement.close();
        connection.close();
    }

    @SneakyThrows
    public void end() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE players SET tutorial = ? WHERE uuid = ?");
        statement.setInt(1, 0);
        statement.setString(2, player.getUniqueId().toString());
        statement.execute();

        statement.close();
        connection.close();
    }
}
