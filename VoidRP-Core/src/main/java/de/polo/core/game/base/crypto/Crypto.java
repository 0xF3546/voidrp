package de.polo.core.game.base.crypto;

import de.polo.core.Main;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.utils.Utils;
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
        // Versuch die Verbindung automatisch zu schließen
        try (Connection connection = Main.getInstance().coreDatabase.getConnection()) {

            // Berechnung der gesamten Markt-Coins
            float totalMarketCoins = 0; // Wird hier initialisiert, um außerhalb der Try-Blocks zugänglich zu sein.
            try (PreparedStatement marketStmt = connection.prepareStatement("SELECT SUM(amount) FROM crypto_market");
                 ResultSet marketRs = marketStmt.executeQuery()) {
                if (marketRs.next()) {
                    totalMarketCoins = marketRs.getFloat(1);
                }
            }

            // Berechnung der Gesamtanzahl der Transaktionen
            int totalTransactions = 0; // Auch außerhalb der Try-Blocks verfügbar.
            try (PreparedStatement transactionsStmt = connection.prepareStatement("SELECT COUNT(*) FROM crypto_transactions");
                 ResultSet transactionsRs = transactionsStmt.executeQuery()) {
                if (transactionsRs.next()) {
                    totalTransactions = transactionsRs.getInt(1);
                }
            }

            // Berechnung der Gesamtanzahl an Coins der Spieler
            float totalPlayerCoins = 0; // Wird hier initialisiert, um außerhalb der Try-Blocks zugänglich zu sein.
            try (PreparedStatement playerCoinsStmt = connection.prepareStatement("SELECT SUM(crypto) FROM players");
                 ResultSet playerCoinsRs = playerCoinsStmt.executeQuery()) {
                if (playerCoinsRs.next()) {
                    totalPlayerCoins = playerCoinsRs.getFloat(1);
                }
            }

            // Berechnung der Preisänderung basierend auf den Werten
            float percentageChange = (price - 0.5f) * 0.1f;
            price += percentageChange;
            if (price < 1) price = 0;

            // Preis-Anpassungen basierend auf Gesamt-Spieler-Coins und Markt-Coins
            if (totalPlayerCoins > 10000) {
                price *= 1.2F;
            } else if (totalMarketCoins < 500) {
                price *= 0.8F;
            }
        } // Alle Ressourcen werden automatisch geschlossen
    }



    @EventHandler
    public void onMinute(MinuteTickEvent event) {
        if (Utils.random(0, 60) == event.getMinute()) updatePrice();
    }
}
