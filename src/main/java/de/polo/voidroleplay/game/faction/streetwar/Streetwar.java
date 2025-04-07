package de.polo.voidroleplay.game.faction.streetwar;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.faction.entity.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.database.impl.CoreDatabase;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.agreement.services.VertragUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import java.util.HashMap;
import java.util.List;
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
        CoreDatabase coreDatabase = Main.getInstance().coreDatabase;
        Statement statement = coreDatabase.getStatement();
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

    public static boolean isInStreetwar(String faction) {
        for (StreetwarData streetwarData : streetwarDataMap.values()) {
            if (streetwarData.getAttacker().equalsIgnoreCase(faction) || streetwarData.getDefender().equalsIgnoreCase(faction)) {
                return true;
            }
        }
        return false;
    }

    public void addPunkte(String faction, int points, String reason) {
        for (StreetwarData streetwarData : streetwarDataMap.values()) {
            if (streetwarData.getAttacker().equalsIgnoreCase(faction)) {
                if (factionManager.getOnlineMemberCount(streetwarData.getAttacker()) < 3) {
                    return;
                }
                streetwarData.setAttacker_points(streetwarData.getAttacker_points() + points);
                factionManager.sendCustomMessageToFaction(streetwarData.getAttacker(), "§8[§6Streetwar§8]§e +" + points + " Punkte §8→ §e" + reason + "§8[§e" + streetwarData.getAttacker_points() + "§7/§6450§8]");
            }
            if (streetwarData.getDefender().equalsIgnoreCase(faction)) {
                if (factionManager.getOnlineMemberCount(streetwarData.getDefender()) < 3) {
                    return;
                }
                streetwarData.setDefender_points(streetwarData.getDefender_points() + points);
                factionManager.sendCustomMessageToFaction(streetwarData.getDefender(), "§8[§6Streetwar§8]§e +" + points + " Punkte §8→ §e" + reason + "§8[§e" + streetwarData.getDefender_points() + "§7/§6450§8]");
            }
            if (streetwarData.getAttacker_points() >= 100 || streetwarData.getDefender_points() >= 100) {
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
        Statement statement = Main.getInstance().coreDatabase.getStatement();
        statement.execute("DELETE FROM streetwar WHERE id = " + id);
        streetwarDataMap.remove(id);
    }

    public void acceptStreetwar(Player player, String attackerFaction) {
        CoreDatabase coreDatabase = Main.getInstance().coreDatabase;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        FactionData attackerData = factionManager.getFactionData(attackerFaction);
        factionManager.sendCustomMessageToFaction(attackerFaction, "§8[§6Streetwar§8]§a Die Fraktion " + factionData.getFullname() + " hat den Streetwar-Antrag angenommen.");
        factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§6Streetwar§8]§a " + player.getName() + " hat den Streetwar-Antrag gegen " + attackerData.getFullname() + " angenommen.");
        String query = "INSERT INTO streetwar (attacker, defender) VALUES (?, ?)";
        try (PreparedStatement statement = coreDatabase.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String
            s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!playerData.isLeader()) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (!factionData.canDoGangwar()) {
            player.sendMessage(Prefix.ERROR + "Deine Fraktion kann keinen Streetwar starten.");
            return false;
        }
        if (args.length < 1) {
            if (!isInStreetwar(factionData.getName())) {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /streetwar [Fraktion]");
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
        List<Player> availablePlayers = new ObjectArrayList<>();
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playersData = playerManager.getPlayerData(players.getUniqueId());
            if (playersData.getFaction().equalsIgnoreCase(args[0]) && playersData.getFactionGrade() >= 7) {
                availablePlayers.add(players);
            }
        }
        if (availablePlayers.isEmpty()) {
            player.sendMessage(Prefix.ERROR + "Es ist kein Fraktionsleader der Gegner-Partei online.");
            return false;
        }
        FactionData defenderData = factionManager.getFactionData(playerManager.getPlayerData(availablePlayers.get(0).getUniqueId()).getFaction());
        if (!defenderData.canDoGangwar()) {
            player.sendMessage(Prefix.ERROR + "Die Gegner-Partei kann keinen Streetwar starten.");
            return false;
        }
        if (playerData.getFaction().equalsIgnoreCase(args[0])) {
            player.sendMessage(Prefix.ERROR + "Du kannst keinen Streetwar gegen deine eigene Fraktion starten.");
            return false;
        }
        for (StreetwarData streetwarData : streetwarDataMap.values()) {
            if (streetwarData.getDefender().equalsIgnoreCase(args[0]) || streetwarData.getAttacker().equalsIgnoreCase(args[0])) {
                player.sendMessage(Prefix.ERROR + "Die Gegner-Partei befindet sich bereits im Streetwar.");
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
            player.sendMessage(Prefix.ERROR + "Es befindet sich kein Fraktionsleader in deiner nähe.");
            return false;
        }
        player.sendMessage("§8[§6Streetwar§8]§a Du hast die Fraktion zum Streetwar herausgefordert.");
        return false;
    }
}
