package de.polo.voidroleplay.utils.player;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.enums.FFAStatsType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerFFAStatsManager {
    private final List<PlayerFFAStats> stats = new ObjectArrayList<>();

    private final Player player;

    public PlayerFFAStatsManager(Player player) {
        this.player = player;
        for (PlayerFFAStats playerFFAStats : Main.getInstance().gamePlay.getFfa().getStats()) {
            if (!playerFFAStats.getUuid().equals(player.getUniqueId().toString())) continue;
            stats.add(playerFFAStats);
        }
    }

    @SneakyThrows
    public void addStats(PlayerFFAStats stats, boolean save) {
        this.stats.add(stats);
        Main.getInstance().gamePlay.getFfa().addPlayerStats(stats);
        if (save) {
            Connection connection = Main.getInstance().coreDatabase.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO player_ffa_stats (uuid, statsType) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, stats.getUuid());
            statement.setString(2, stats.getFfaStatsType().name());
            statement.execute();
            ResultSet result = statement.getGeneratedKeys();
            if (result.next()) {
                stats.setId(result.getInt(1));
            }
            statement.close();
            connection.close();
        }
    }

    public void save() {
        try (Connection connection = Main.getInstance().coreDatabase.getConnection()) {
            for (PlayerFFAStats stat : stats) {
                if (stat.getId() > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE player_ffa_stats SET statsType = ?, kills = ?, deaths = ? WHERE id = ?")) {
                        statement.setString(1, stat.getFfaStatsType().name());
                        statement.setInt(2, stat.getKills());
                        statement.setInt(3, stat.getDeaths());
                        statement.setInt(4, stat.getId());
                        statement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO player_ffa_stats (uuid, statsType, kills, deaths) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        statement.setString(1, stat.getUuid());
                        statement.setString(2, stat.getFfaStatsType().name());
                        statement.setInt(3, stat.getKills());
                        statement.setInt(4, stat.getDeaths());
                        statement.executeUpdate();
                        try (ResultSet result = statement.getGeneratedKeys()) {
                            if (result.next()) {
                                stat.setId(result.getInt(1));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Collection<PlayerFFAStats> getStats() {
        return stats;
    }

    public PlayerFFAStats getStats(FFAStatsType type) {
        return stats.stream()
                .filter(s -> s.getFfaStatsType().equals(type))
                .findFirst()
                .orElse(null);
    }

    private void checkForUncreatedStat() {
        for (FFAStatsType type : FFAStatsType.values()) {
            if (getStats(type) != null) continue;
            PlayerFFAStats playerFFAStats = new PlayerFFAStats(player.getUniqueId().toString(), 0, 0);
            playerFFAStats.setFfaStatsType(type);
            addStats(playerFFAStats, true);
        }
    }

    public void addKill() {
        for (PlayerFFAStats stat : stats) {
            stat.setKills(stat.getKills() + 1);
        }
    }

    public void addDeath() {
        for (PlayerFFAStats stat : stats) {
            stat.setDeaths(stat.getDeaths() + 1);
        }
    }

    public void handleJoin() {
        checkForUncreatedStat();
    }

    public void clearStats(FFAStatsType type) {
        for (PlayerFFAStats ffaStats : stats) {
            if (ffaStats.getFfaStatsType().equals(type)) stats.remove(ffaStats);
        }
    }
}
