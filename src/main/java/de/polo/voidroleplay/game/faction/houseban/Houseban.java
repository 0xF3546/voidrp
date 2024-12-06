package de.polo.voidroleplay.game.faction.houseban;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Houseban implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final List<PlayerHouseban> housebans = new ArrayList<>();

    public Houseban(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("houseban", this);

        loadAll();
    }

    @SneakyThrows
    private void loadAll() {
        housebans.clear();
        PreparedStatement statement = Main.getInstance().mySQL.getConnection().prepareStatement("SELECT * FROM housebans");
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            PlayerHouseban playerHouseban = new PlayerHouseban(UUID.fromString(result.getString("uuid")), result.getString("reason"), Utils.toLocalDateTime(result.getDate("until")));
            playerHouseban.setId(result.getInt("id"));
            playerHouseban.setPunisher(UUID.fromString(result.getString("punisher")));
            housebans.add(playerHouseban);
        }
        statement.close();
    }

    public PlayerHouseban getByUuid(UUID uuid) {
        for (PlayerHouseban houseban : housebans) {
            if (houseban.getUuid().equals(uuid)) return houseban;
        }
        return null;
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!factionManager.isPlayerInGoodFaction(player)) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage("§7   ===§8[§cHausverbote§8]§7===");
            for (PlayerHouseban playerHouseban : housebans) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerHouseban.getUuid());
                if (offlinePlayer.isOnline()) {
                    player.sendMessage("§8 ➥ §a" + offlinePlayer.getName() + " §8| §7" + Utils.localDateTimeToReadableString(playerHouseban.getUntil()) + " §8 | §7" + playerHouseban.getReason());
                } else {
                    player.sendMessage("§8 ➥ §c" + offlinePlayer.getName() + " §8| §7" + Utils.localDateTimeToReadableString(playerHouseban.getUntil()) + " §8 | §7" + playerHouseban.getReason());
                }
            }
            return false;
        }
        if (args[0].equalsIgnoreCase("remove")) {
            if (playerData.getFactionGrade() < 4) {
                player.sendMessage(Prefix.ERROR_NOPERMISSION);
                return false;
            }
            if (args.length < 2) {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /houseban remove [Spieler]");
                return false;
            }
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (offlinePlayer.getName() == null) continue;
                if (offlinePlayer.getName().equalsIgnoreCase(args[1])) {
                    try {
                        PlayerHouseban houseban = getByUuid(offlinePlayer.getUniqueId());
                        if (houseban == null) {
                            player.sendMessage(Prefix.ERROR + "Der Spieler hat kein Hausverbot.");
                            return false;
                        }
                        factionManager.sendCustomMessageToFactions("§8[§cHausverbot§8]§7 " + player.getName() + " das Hausverbot von " + offlinePlayer.getName() + " aufgehoben.", "Medic", "FBI", "Polizei");
                        housebans.remove(houseban);
                        Connection connection = Main.getInstance().mySQL.getConnection();
                        PreparedStatement statement = connection.prepareStatement("DELETE FROM housebans WHERE id = ?");
                        statement.setInt(1, houseban.getId());
                        statement.execute();
                        statement.close();
                        connection.close();
                    } catch (Exception ex) {
                        player.sendMessage(Prefix.ERROR + "Die Zeit muss numerisch sein.");
                    }
                    return false;
                }
            }
            player.sendMessage(Prefix.ERROR + "Der Spieler wurde nicht gefunden.");
        }
        if (args.length < 3) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /houseban [add/remove] [Spieler] [Zeit (Tage)] [Grund]");
            return false;
        }
        if (args[0].equalsIgnoreCase("add")) {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (offlinePlayer.getName() == null) continue;
                if (offlinePlayer.getName().equalsIgnoreCase(args[1])) {
                    if (getByUuid(offlinePlayer.getUniqueId()) != null) {
                        player.sendMessage(Prefix.ERROR + "Der Spieler hat bereits Hausverbot.");
                        return false;
                    }
                    try {
                        int time = Integer.parseInt(args[2]);
                        args[0] = "";
                        args[1] = "";
                        args[2] = "";
                        StringBuilder reason = new StringBuilder(args[2]);
                        for (int i = 3; i < args.length; i++) {
                            reason.append(" ").append(args[i]);
                        }
                        factionManager.sendCustomMessageToFactions("§8[§cHausverbot§8]§7 " + player.getName() + " hat " + offlinePlayer.getName() + " für " + time + " Tage Hausverbot erteilt. Grund: " + reason, "Medic", "FBI", "Polizei");
                        LocalDateTime t = Utils.getTime();
                        t = t.plusDays(time);
                        PlayerHouseban playerHouseban = new PlayerHouseban(offlinePlayer.getUniqueId(), reason.toString(), t);
                        Connection connection = Main.getInstance().mySQL.getConnection();
                        PreparedStatement statement = connection.prepareStatement("INSERT INTO housebans (uuid, punisher, reason, until) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                        statement.setString(1, offlinePlayer.getUniqueId().toString());
                        statement.setString(2, player.getUniqueId().toString());
                        statement.setString(3, reason.toString());
                        statement.setString(4, Utils.localDateTimeToString(t));
                        statement.execute();
                        ResultSet generatedKeys = statement.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int key = generatedKeys.getInt(1);
                            playerHouseban.setId(key);
                        }
                        housebans.add(playerHouseban);
                        statement.close();
                        connection.close();
                    } catch (Exception ex) {
                        player.sendMessage(Prefix.ERROR + "Die Zeit muss numerisch sein.");
                    }
                    return false;
                }
            }
            player.sendMessage(Prefix.ERROR + "Der Spieler wurde nicht gefunden.");
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        return null;
    }
}
