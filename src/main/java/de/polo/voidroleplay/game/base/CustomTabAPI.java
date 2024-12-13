package de.polo.voidroleplay.game.base;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import net.kyori.adventure.text.Component;

public class CustomTabAPI {

    // Rank storage
    private Map<String, RankData> ranks = new HashMap<>();
    // Player to rank mapping
    private Map<UUID, String> playerRanks = new HashMap<>();
    // Tab header & footer lines
    private List<String> tabHeaderLines = new ArrayList<>();
    private List<String> tabFooterLines = new ArrayList<>();

    // A special set of viewers who will see different prefixes
    private Set<UUID> specialViewers = new HashSet<>();

    public CustomTabAPI() {
        // Nothing special needed in constructor now
    }

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
     * Adds a player to the special viewers list.
     * Special viewers may see different prefixes for certain players.
     */
    public void addSpecialViewer(Player player) {
        specialViewers.add(player.getUniqueId());
    }

    /**
     * Removes a player from the special viewers list.
     */
    public void removeSpecialViewer(Player player) {
        specialViewers.remove(player.getUniqueId());
    }

    /**
     * Checks if a player is a special viewer.
     */
    public boolean isSpecialViewer(Player player) {
        return specialViewers.contains(player.getUniqueId());
    }

    /**
     * Determines the prefix for a target player based on who is viewing.
     *
     * Example logic:
     * If the viewer is in the special viewers set, they see "X" prefix for the target,
     * else they see the normal prefix defined by the target's rank.
     *
     * You can customize this logic further as needed.
     */
    private String getPrefixFor(Player viewer, Player target) {
        String rankName = playerRanks.get(target.getUniqueId());
        RankData data = rankName != null ? ranks.get(rankName) : null;
        String basePrefix = (data != null) ? data.prefix : "§7"; // default prefix if none is set

        // Example: If viewer is special, add a star to indicate a special view
        if (isSpecialViewer(viewer)) {
            // For demonstration, we prepend "§b★ " to the existing prefix for special viewers
            String specialPrefix = "§b★ " + basePrefix;
            if (specialPrefix.length() > 16) {
                specialPrefix = specialPrefix.substring(0, 16);
            }
            return specialPrefix;
        } else {
            // Normal viewers see the normal prefix
            return basePrefix;
        }
    }

    /**
     * Updates the tab for all players.
     * This now creates a unique scoreboard per viewer and sets up teams and prefixes accordingly.
     */
    public void updateTabForAllPlayers() {
        // Get sorted ranks by priority for reference
        List<Map.Entry<String, RankData>> sortedRanks = ranks.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().priority))
                .collect(Collectors.toList());

        // For each viewer, we create a scoreboard
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager == null) continue; // Should not normally happen
            Scoreboard sb = manager.getNewScoreboard();

            // We'll create teams dynamically based on what prefixes we need.
            // Because each viewer can see different prefixes, the number of
            // unique prefixes might vary.
            // We'll map prefix -> team to avoid creating duplicate teams for the same prefix.
            Map<String, Team> prefixTeams = new HashMap<>();

            // Assign each online player (target) a team on the viewer's scoreboard
            for (Player target : Bukkit.getOnlinePlayers()) {
                String prefix = getPrefixFor(viewer, target);
                Team team = prefixTeams.get(prefix);
                if (team == null) {
                    // Create a team for this prefix
                    // We must ensure the team name is unique and <= 16 chars.
                    // Use a hash of the prefix or a simple counter.
                    String teamName = generateTeamName(prefix, prefixTeams.size());
                    team = sb.registerNewTeam(teamName);
                    team.prefix(Component.text(prefix));
                    team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                    prefixTeams.put(prefix, team);
                }
                team.addEntry(target.getName());
            }

            // Apply scoreboard to the viewer
            viewer.setScoreboard(sb);

            // Update tab header & footer for the viewer
            Component header = Component.text(formatTabLines(tabHeaderLines));
            Component footer = Component.text(formatTabLines(tabFooterLines));
            viewer.sendPlayerListHeaderAndFooter(header, footer);
        }
    }

    /**
     * Utility method to generate a unique team name from a prefix.
     */
    private String generateTeamName(String prefix, int index) {
        // Just use "T" + index. This avoids length issues and special chars.
        String teamName = "T" + index;
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }
        return teamName;
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
