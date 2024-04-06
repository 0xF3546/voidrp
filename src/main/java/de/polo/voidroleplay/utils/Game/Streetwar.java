package de.polo.voidroleplay.utils.Game;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.StreetwarData;
import de.polo.voidroleplay.database.MySQL;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.VertragUtil;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Streetwar implements CommandExecutor {
    public static Map<Integer, StreetwarData> streetwarDataMap = new HashMap<>();
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;

    public Streetwar(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        load();
        Main.registerCommand("streetwar", this);
    }

    @SneakyThrows
    public static void load() {
        MySQL mySQL = Main.getInstance().mySQL;
        Statement statement = mySQL.getStatement();
        ResultSet res = statement.executeQuery("SELECT * FROM streetwar");
        while (res.next()) {
            StreetwarData streetwarData = new StreetwarData();
            streetwarData.setId(res.getInt("id"));
            streetwarData.setAttacker(res.getString("attacker"));
            streetwarData.setDefender(res.getString("defender"));
            streetwarData.setAttacker_points(res.getInt("attacker_points"));
            streetwarData.setDefender_points(res.getInt("defender_points"));
            streetwarData.setStarted(Utils.toLocalDateTime(res.getDate("started")));
            streetwarDataMap.put(res.getInt(1), streetwarData);
        }
    }

    public void addPunkte(String faction, int points, String reason) {
        for (StreetwarData streetwarData : streetwarDataMap.values()) {
            if (streetwarData.getAttacker().equalsIgnoreCase(faction)) {
                streetwarData.setAttacker_points(streetwarData.getAttacker_points() + points);
                factionManager.sendCustomMessageToFaction(streetwarData.getAttacker(), "§8[§6Streetwar§8]§e +" + points + " Punkte §8→ §e" + reason + "§8[§e" + streetwarData.getAttacker_points() + "§7/§6450§8]");
            }
            if (streetwarData.getDefender().equalsIgnoreCase(faction)) {
                streetwarData.setDefender_points(streetwarData.getDefender_points() + points);
                factionManager.sendCustomMessageToFaction(streetwarData.getDefender(), "§8[§6Streetwar§8]§e +" + points + " Punkte §8→ §e" + reason + "§8[§e" + streetwarData.getDefender_points() + "§7/§6450§8]");
            }
            if (streetwarData.getAttacker_points() >= 450 || streetwarData.getDefender_points() >= 450) {
                endStreetwar(streetwarData.getId());
            } else {
                streetwarData.save();
            }
        }
    }

    @SneakyThrows
    public void endStreetwar(int id) {
        StreetwarData streetwarData = null;
        for (StreetwarData sd : streetwarDataMap.values()) {
            if (sd.getId() == id) {
                streetwarData = sd;
            }
        }
        if (streetwarData == null) return;
        String winner = null;
        Bukkit.broadcastMessage("");
        if (streetwarData.getDefender_points() >= 450) {
            winner = streetwarData.getDefender();
            FactionData factionData = factionManager.getFactionData(streetwarData.getDefender());
            FactionData loserFaction = factionManager.getFactionData(streetwarData.getAttacker());
            Bukkit.broadcastMessage("§8[§6§lSTREETWAR§8]§e Die Fraktion §6" + factionData.getFullname() + "§e hat den Streetwar gegen die Fraktion §6" + loserFaction.getFullname() + "§e mit §6" + streetwarData.getDefender_points() + " zu " + streetwarData.getAttacker_points() + "§e gewonnen!");
        } else if (streetwarData.getAttacker_points() >= 450) {
            winner = streetwarData.getAttacker();
            FactionData factionData = factionManager.getFactionData(streetwarData.getDefender());
            FactionData loserFaction = factionManager.getFactionData(streetwarData.getAttacker());
            Bukkit.broadcastMessage("§8[§6§lSTREETWAR§8]§e Die Fraktion §6" + loserFaction.getFullname() + "§e hat den Streetwar gegen die Fraktion §6" + factionData.getFullname() + "§e mit §6" + streetwarData.getAttacker_points() + " zu " + streetwarData.getDefender_points() + "§e gewonnen!");
        }
        Bukkit.broadcastMessage("");
        Statement statement = Main.getInstance().mySQL.getStatement();
        statement.execute("DELETE FROM streetwar WHERE id = " + id);
        streetwarDataMap.remove(id);
    }

    public void acceptStreetwar(Player player, String attackerFaction) {
        MySQL mySQL = Main.getInstance().mySQL;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        FactionData attackerData = factionManager.getFactionData(attackerFaction);
        factionManager.sendCustomMessageToFaction(attackerFaction, "§8[§6Streetwar§8]§a Die Fraktion " + factionData.getFullname() + " hat den Streetwar-Antrag angenommen.");
        factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§6Streetwar§8]§a " + player.getName() + " hat den Streetwar-Antrag gegen " + attackerData.getFullname() + " angenommen.");
        String query = "INSERT INTO streetwar (attacker, defender) VALUES (?, ?)";
        try (PreparedStatement statement = mySQL.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, attackerData.getName());
            statement.setString(2, factionData.getName());
            statement.execute();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                StreetwarData streetwarData = new StreetwarData();
                streetwarData.setAttacker(attackerData.getName());
                streetwarData.setAttacker_points(0);
                streetwarData.setDefender(factionData.getName());
                streetwarData.setDefender_points(0);
                streetwarData.setStarted(LocalDateTime.now());
                streetwarData.setId(generatedKeys.getInt(1));
                streetwarDataMap.put(generatedKeys.getInt(1), streetwarData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8[§6§lSTREETWAR§8]§e Die Fraktion " + attackerData.getName() + " hat einen Streetwar gegen die Fraktion " + factionData.getName() + " gestartet!");
        Bukkit.broadcastMessage("");
    }

    public void denyStreetwar(Player player, String attackerFaction) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        FactionData attackerData = factionManager.getFactionData(attackerFaction);
        factionManager.sendCustomMessageToFaction(attackerFaction, "§8[§6Streetwar§8]§c Die Fraktion " + factionData.getFullname() + " hat den Streetwar-Antrag abgelehnt.");
        factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§6Streetwar§8]§c " + player.getName() + " hat den Streetwar-Antrag gegen " + attackerData.getFullname() + " abgelehnt.");
    }

    public static boolean isInStreetwar(String faction) {
        for (StreetwarData streetwarData : streetwarDataMap.values()) {
            if (streetwarData.getAttacker().equalsIgnoreCase(faction) || streetwarData.getDefender().equalsIgnoreCase(faction)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String
            s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFactionGrade() < 7) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (!factionData.canDoGangwar()) {
            player.sendMessage(Main.error + "Deine Fraktion kann keinen Streetwar starten.");
            return false;
        }
        if (args.length < 1) {
            if (!isInStreetwar(factionData.getName())) {
                player.sendMessage(Main.error + "Syntax-Fehler: /streetwar [Fraktion]");
                return false;
            }
            for (StreetwarData streetwarData : streetwarDataMap.values()) {
                if (streetwarData.getDefender().equalsIgnoreCase(factionData.getName()) || streetwarData.getAttacker().equalsIgnoreCase(factionData.getName())) {
                    player.sendMessage("§8[§6Streetwar§8]§e " + streetwarData.getAttacker() + " §8× §e" + streetwarData.getAttacker_points() + " vs " + streetwarData.getDefender_points() + " §8× §e" + streetwarData.getDefender());
                    return false;
                }
            }
            return false;
        }
        ArrayList<Player> availablePlayers = new ArrayList<>();
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playersData = playerManager.getPlayerData(players.getUniqueId());
            if (playersData.getFaction().equalsIgnoreCase(args[0]) && playersData.getFactionGrade() >= 7) {
                availablePlayers.add(players);
            }
        }
        if (availablePlayers.size() == 0) {
            player.sendMessage(Main.error + "Es ist kein Fraktionsleader der Gegner-Partei online.");
            return false;
        }
        FactionData defenderData = factionManager.getFactionData(playerManager.getPlayerData(availablePlayers.get(0).getUniqueId()).getFaction());
        if (!defenderData.canDoGangwar()) {
            player.sendMessage(Main.error + "Die Gegner-Partei kann keinen Streetwar starten.");
            return false;
        }
        if (playerData.getFaction().equalsIgnoreCase(args[0])) {
            player.sendMessage(Main.error + "Du kannst keinen Streetwar gegen deine eigene Fraktion starten.");
            return false;
        }
        for (StreetwarData streetwarData : streetwarDataMap.values()) {
            if (streetwarData.getDefender().equalsIgnoreCase(args[0]) || streetwarData.getAttacker().equalsIgnoreCase(args[0])) {
                player.sendMessage(Main.error + "Die Gegner-Partei befindet sich bereits im Streetwar.");
                return false;
            }
        }
        boolean sendMessage = false;
        for (Player leader : availablePlayers) {
            if (player.getLocation().distance(leader.getLocation()) < 5) {
                leader.sendMessage("§8[§6Streetwar§8]§c " + factionManager.getPlayerFactionRankName(player) + " " + player.getName() + " hat deine Fraktion zum Streetwar herausgefordert.");
                utils.vertragUtil.sendInfoMessage(leader);
                VertragUtil.setVertrag(player, leader, "streetwar", factionData.getName());
                sendMessage = true;
            }
        }
        if (!sendMessage) {
            player.sendMessage(Main.error + "Es befindet sich kein Fraktionsleader in deiner nähe.");
            return false;
        }
        player.sendMessage("§8[§6Streetwar§8]§a Du hast die Fraktion zum Streetwar herausgefordert.");
        return false;
    }
}
