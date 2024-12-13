package de.polo.voidroleplay.game;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import net.kyori.adventure.text.Component;

public class CustomTabAPI {

    private ScoreboardManager scoreboardManager;
    private Scoreboard scoreboard;
    
    // Map<RankName, RankData>
    // RankData holds the prefix and priority
    private Map<String, RankData> ranks = new HashMap<>();
    
    // Map<PlayerUUID, RankName>
    private Map<UUID, String> playerRanks = new HashMap<>();
    
    // Tab header & footer lines
    private List<String> tabHeaderLines = new ArrayList<>();
    private List<String> tabFooterLines = new ArrayList<>();
    
    public CustomTabAPI() {
        this.scoreboardManager = Bukkit.getScoreboardManager();
        this.scoreboard = scoreboardManager.getNewScoreboard();
    }
    
    /**
     * Data class for rank information
     */
    private static class RankData {
        String prefix;
        int priority;
        
        RankData(String prefix, int priority) {
            this.prefix = prefix;
            this.priority = priority;
        }
    }
    
    /**
     * Adds or updates a rank.
     *
     * @param rankName The name of the rank (unique identifier)
     * @param prefix The prefix for the rank, use § for color codes
     * @param priority Lower number means higher priority in the tab list
     */
    public void setRank(String rankName, String prefix, int priority) {
        // The prefix may not exceed 16 characters due to scoreboard limitations
        if (prefix.length() > 16) {
            prefix = prefix.substring(0, 16);
        }
        ranks.put(rankName.toLowerCase(), new RankData(prefix, priority));
    }
    
    /**
     * Assigns a previously defined rank to a player.
     *
     * @param player The player
     * @param rankName The rank name (must have been added via setRank before)
     */
    public void setPlayerRank(Player player, String rankName) {
        rankName = rankName.toLowerCase();
        if (!ranks.containsKey(rankName)) {
            throw new IllegalArgumentException("Rank " + rankName + " is not defined.");
        }
        playerRanks.put(player.getUniqueId(), rankName);
    }
    
    /**
     * Removes a player's rank assignment.
     *
     * @param player The player
     */
    public void removePlayerRank(Player player) {
        playerRanks.remove(player.getUniqueId());
    }
    
    /**
     * Sets the tab header lines. Use § for color codes.
     * Each entry in the list is a new line.
     */
    public void setTabHeader(List<String> lines) {
        this.tabHeaderLines = new ArrayList<>(lines);
    }
    
    /**
     * Sets the tab footer lines. Use § for color codes.
     * Each entry in the list is a new line.
     */
    public void setTabFooter(List<String> lines) {
        this.tabFooterLines = new ArrayList<>(lines);
    }
    
    /**
     * Updates the tab list for all online players.
     * Should be called after rank or prefix changes.
     */
    public void updateTabForAllPlayers() {
        // Clear existing teams
        for (Team t : scoreboard.getTeams()) {
            t.unregister();
        }
        
        // Sort ranks by priority
        List<Map.Entry<String, RankData>> sortedRanks = ranks.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().priority))
                .toList();
        
        // Create teams for each rank
        for (Map.Entry<String, RankData> entry : sortedRanks) {
            String teamName = entry.getValue().priority + entry.getKey();
            if (teamName.length() > 16) {
                teamName = teamName.substring(0, 16);
            }
            
            Team team = scoreboard.registerNewTeam(teamName);
            team.prefix(Component.text(entry.getValue().prefix));
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        }
        
        // Assign players to teams based on their rank
        for (Player player : Bukkit.getOnlinePlayers()) {
            String rankName = playerRanks.get(player.getUniqueId());
            if (rankName != null && ranks.containsKey(rankName)) {
                RankData data = ranks.get(rankName);
                String teamName = data.priority + rankName;
                Team team = scoreboard.getTeam(teamName);
                if (team != null) {
                    team.addEntry(player.getName());
                }
            }
            player.setScoreboard(scoreboard);
        }
        
        // Update tab header and footer
        Component header = Component.text(formatTabLines(tabHeaderLines));
        Component footer = Component.text(formatTabLines(tabFooterLines));
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendPlayerListHeaderAndFooter(header, footer);
        }
    }
    
    /**
     * Utility method to format tab lines and replace placeholders.
     */
    private String formatTabLines(List<String> lines) {
        String result = String.join("\n", lines);
        result = result.replaceAll("&", "§");
        result = result.replace("%onlineplayers%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        result = result.replace("%maxplayers%", String.valueOf(Bukkit.getMaxPlayers()));
        return result;
    }
    
    /**
     * Checks if a player is online by UUID.
     */
    public boolean isPlayerOnline(UUID playerUUID) {
        Player p = Bukkit.getPlayer(playerUUID);
        return p != null && p.isOnline();
    }
}
