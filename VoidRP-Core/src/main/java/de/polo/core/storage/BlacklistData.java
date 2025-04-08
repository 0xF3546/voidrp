package de.polo.core.storage;

import de.polo.core.Main;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class BlacklistData {
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private String uuid;
    @Getter
    @Setter
    private String faction;
    @Getter
    @Setter
    private String reason;
    @Getter
    @Setter
    private String date;
    @Getter
    @Setter
    private int kills;
    @Getter
    @Setter
    private int price;

    @SneakyThrows
    public void save() {
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE blacklist SET reason = ?, kills = ?, price = ? WHERE id = ?");
        statement.setString(1, reason);
        statement.setInt(2, kills);
        statement.setInt(3, price);
        statement.setInt(4, id);
        statement.executeUpdate();
        statement.close();
        connection.close();
    }
}
