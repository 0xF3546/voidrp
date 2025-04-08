package de.polo.core.utils.player;

import de.polo.core.Main;
import de.polo.core.utils.enums.Powerup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerPowerUp {

    @Getter
    private final Powerup powerup;
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private int amount;

    @Getter
    @Setter
    private int upgrades;

    public PlayerPowerUp(Powerup powerup) {
        this.powerup = powerup;
    }

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE player_powerups SET amount = ?, upgrades = ? WHERE id = ?");
        statement.setInt(1, amount);
        statement.setInt(2, upgrades);
        statement.setInt(3, id);
        statement.execute();
        statement.close();
        connection.close();
    }
}
