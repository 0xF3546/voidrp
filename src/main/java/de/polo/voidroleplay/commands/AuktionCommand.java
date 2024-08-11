package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.*;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class AuktionCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final LocationManager locationManager;

    public AuktionCommand(PlayerManager playerManager, FactionManager factionManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.locationManager = locationManager;

        Main.registerCommand("auktion", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.error_nopermission);
            return false;
        }
        if (playerData.getFactionGrade() < 7) {
            player.sendMessage(Prefix.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /auktion [bet/list] [(Anzahl)]");
            return false;
        }
        if (args[0].equalsIgnoreCase("list")) {
            Map<String, Integer> factionBets = getFactionBets();

            List<Map.Entry<String, Integer>> sortedFactionBets = new ArrayList<>(factionBets.entrySet());
            sortedFactionBets.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
            player.sendMessage("§7   ===§8[§3Bank§8]§7===");
            for (Map.Entry<String, Integer> entry : sortedFactionBets) {
                FactionData fData = factionManager.getFactionData(entry.getKey());
                if (fData == null) continue;
                player.sendMessage("§8 ➥ §" + fData.getPrimaryColor() + fData.getName() + "§8:§a " + Utils.toDecimalFormat(entry.getValue()) + "$");
            }
            return true;
        }
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (locationManager.getDistanceBetweenCoords(player, "bank") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe der Bank.");
            return false;
        }
        try {
            int amount = Integer.parseInt(args[1]);
            if (playerData.getBargeld() < amount) {
                player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei.");
                return false;
            }
            if (amount < 1) {
                player.sendMessage(Prefix.ERROR + "Der Betrag muss mindestens 1 Betragen");
                return false;
            }
            String[] factions = factionManager.getFactions().stream().map(FactionData::getName).toArray(String[]::new);
            factionManager.sendCustomLeaderMessageToFactions("§8[§3Auktion§8]§7 Die Fraktion §" + factionData.getPrimaryColor() + factionData.getFullname() + "§7 haben Ihr Gebot um §a" + Utils.toDecimalFormat(amount) + "$§7 erhöht.", factions);
            factionManager.sendMessageToFaction(playerData.getFaction(), factionManager.getPlayerFactionRankName(player) + " " + player.getName() + " hat das Gebot der Fraktion um " + Utils.toDecimalFormat(amount) + "$ erhöht.");
            playerData.removeMoney(amount, "Auktion - Bet");
            payIn(factionData, amount);
        } catch (Exception ex) {
            player.sendMessage(Prefix.ERROR + "Die Anzahl muss numerisch sein.");
        }
        return false;
    }

    @SneakyThrows
    private Map<String, Integer> getFactionBets() {
        Map<String, Integer> factionBets = new HashMap<>();
        Connection connection = Main.getInstance().mySQL.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT faction, SUM(amount) AS total_amount FROM auction_bets GROUP BY faction");

        while (resultSet.next()) {
            String faction = resultSet.getString("faction");
            int totalAmount = resultSet.getInt("total_amount");
            factionBets.put(faction, totalAmount);
        }

        return factionBets;
    }

    @SneakyThrows
    private void payIn(FactionData factionData, int amount) {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO auction_bets (faction, amount) VALUES (?, ?)");
        statement.setString(1, factionData.getName());
        statement.setInt(2, amount);
        statement.execute();
        statement.close();
        connection.close();
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> value = new ArrayList<>();
            value.add("bet");
            value.add("list");
            return value;
        }
        return null;
    }

    public void rollAuction() {
        Map<String, Integer> factionBets = getFactionBets();
        List<Map.Entry<String, Integer>> sortedFactionBets = new ArrayList<>(factionBets.entrySet());
        sortedFactionBets.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        if (sortedFactionBets.isEmpty()) {
            return;
        }

        Map.Entry<String, Integer> highestBidEntry = sortedFactionBets.get(0);
        String winningFactionName = highestBidEntry.getKey();
        int amount = highestBidEntry.getValue();

        FactionData winningFaction = factionManager.getFactionData(winningFactionName);
        if (winningFaction == null) {
            factionManager.sendCustomLeaderMessageToFactions("§8[§3Auktion§8]§7 Ein Fehler ist aufgetreten: Die gewinnende Fraktion konnte nicht gefunden werden.", new String[]{});
            return;
        }

        String[] factions = factionManager.getFactions().stream().map(FactionData::getName).toArray(String[]::new);
        factionManager.sendCustomLeaderMessageToFactions(
                "§8[§3Auktion§8]§7 Die Fraktion " + winningFaction.getPrimaryColor() + winningFaction.getFullname() +
                        "§7 hat mit einem Einsatz von §a" + amount + "$§7 die Auktion gewonnen!",
                factions
        );

        GlobalStats.setValue("auction", String.valueOf(winningFaction.getId()), true);
        clearList();
    }

    @SneakyThrows
    private void clearList() {
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("DELETE FROM auction_bets");
        statement.execute();
        statement.close();
        connection.close();
    }
}
