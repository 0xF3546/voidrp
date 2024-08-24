package de.polo.voidroleplay.utils.playerUtils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.enums.Powerup;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerPowerUpManager {
    private final List<PlayerPowerUp> powerUps = new ArrayList<>();

    private final Player player;
    private final PlayerData playerData;

    public PlayerPowerUpManager(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        load();
    }

    @SneakyThrows
    private void load() {
        powerUps.clear();
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_powerups WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            PlayerPowerUp powerUp = new PlayerPowerUp(Powerup.valueOf(result.getString("powerup")));
            powerUp.setId(result.getInt("id"));
            powerUp.setUpgrades(result.getInt("upgrades"));
            powerUp.setAmount(result.getInt("amount"));
            powerUps.add(powerUp);
        }
        statement.close();
        result.close();
        connection.close();
    }

    public Collection<PlayerPowerUp> getPowerUps() {
        return powerUps;
    }

    public PlayerPowerUp getPowerUp(Powerup powerup) {
        PlayerPowerUp playerPowerUp = powerUps.stream().filter(p -> p.getPowerup().equals(powerup)).findFirst().orElse(null);
        if (playerPowerUp == null) {
            playerPowerUp = new PlayerPowerUp(powerup);
            createPowerUp(playerPowerUp);
        }
        return playerPowerUp;
    }

    public int getUpgradePrice(Powerup powerup) {
        PlayerPowerUp playerPowerUp = getPowerUp(powerup);
        return powerup.getBaseUpgradePrice() + (playerPowerUp.getUpgrades() * powerup.getIncreaseAmount());
    }

    public boolean upgrade(Powerup powerup) {
        PlayerPowerUp playerPowerUp = getPowerUp(powerup);
        int price = getUpgradePrice(powerup);
        if (playerData.getBargeld() < price) {
            return false;
        }
        if (powerup.getMaxAmount() != -1) {
            if (powerup.getMaxAmount() <= playerPowerUp.getAmount()) {
                return false;
            }
        }
        playerData.removeMoney(price, "Powerup-Kauf: " + powerup.name());
        playerPowerUp.setAmount(playerPowerUp.getAmount() + powerup.getUpgradeAmount());
        playerPowerUp.setUpgrades(playerPowerUp.getUpgrades() + 1);
        playerPowerUp.save();
        return true;
    }

    @SneakyThrows
    private void createPowerUp(PlayerPowerUp playerPowerUp) {
        powerUps.add(playerPowerUp);
        playerPowerUp.setAmount(playerPowerUp.getPowerup().getBaseAmount());
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO player_powerups (uuid, powerup, amount) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, playerPowerUp.getPowerup().name());
        statement.setInt(3, playerPowerUp.getPowerup().getBaseAmount());
        statement.execute();
        ResultSet result = statement.getGeneratedKeys();
        if (result.next()) {
            playerPowerUp.setId(result.getInt(1));
        }
        statement.close();
        connection.close();
    }
}
