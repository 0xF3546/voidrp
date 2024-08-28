package de.polo.voidroleplay.game.base.crypto;

import de.polo.voidroleplay.Main;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class Miner {

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private float kWh;

    @Getter
    @Setter
    private float coins;

    @Getter
    @Setter
    private boolean active;

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE crypto_miner SET kWh = ?, coins = ?, active = ? WHERE id = ?");
        statement.setFloat(1, kWh);
        statement.setFloat(2, coins);
        statement.setBoolean(3, active);
        statement.setInt(4, id);
        statement.execute();
        statement.close();
        connection.close();
    }
}
