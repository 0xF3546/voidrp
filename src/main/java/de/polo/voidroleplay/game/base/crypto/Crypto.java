package de.polo.voidroleplay.game.base.crypto;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.events.MinuteTickEvent;
import de.polo.voidroleplay.utils.Utils;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Crypto implements Listener {

    @Getter
    private float price = Utils.random(1, 200);

    public Crypto() {
        updatePrice();
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @SneakyThrows
    private void updatePrice() {
        Connection connection = Main.getInstance().coreDatabase.getConnection();

        PreparedStatement marketStmt = connection.prepareStatement("SELECT SUM(amount) FROM crypto_market");
        ResultSet marketRs = marketStmt.executeQuery();
        float totalMarketCoins = 0;
        if (marketRs.next()) {
            totalMarketCoins = marketRs.getFloat(1);
        }

        PreparedStatement transactionsStmt = connection.prepareStatement("SELECT COUNT(*) FROM crypto_transactions");
        ResultSet transactionsRs = transactionsStmt.executeQuery();
        int totalTransactions = 0;
        if (transactionsRs.next()) {
            totalTransactions = transactionsRs.getInt(1);
        }

        PreparedStatement playerCoinsStmt = connection.prepareStatement("SELECT SUM(crypto) FROM players");
        ResultSet playerCoinsRs = playerCoinsStmt.executeQuery();
        float totalPlayerCoins = 0;
        if (playerCoinsRs.next()) {
            totalPlayerCoins = playerCoinsRs.getFloat(1);
        }

        float percentageChange = (price - 0.5f) * 0.1f;
        price += percentageChange;
        if (price < 1) price = 0;

        if (totalPlayerCoins > 10000) {
            price *= 1.2F;
        } else if (totalMarketCoins < 500) {
            price *= 0.8F;
        }

        marketStmt.close();
        transactionsStmt.close();
        playerCoinsStmt.close();
    }


    @EventHandler
    public void onMinute(MinuteTickEvent event) {
        if (Utils.random(0, 60) == event.getMinute()) updatePrice();
    }
}
