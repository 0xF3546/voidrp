package de.polo.voidroleplay.utils.gameplay;

import de.polo.voidroleplay.storage.BlacklistData;
import de.polo.voidroleplay.storage.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.utils.player.ScoreboardAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

public class DisplayNameManager {

    private final Map<Player, String> playerScoreboards = new HashMap<>();
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Map<String, BlacklistData> blacklistMap = new HashMap<>();
    private final ScoreboardAPI scoreboardAPI;

    public DisplayNameManager(PlayerManager playerManager, FactionManager factionManager, ScoreboardAPI scoreboardAPI) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.scoreboardAPI = scoreboardAPI;
        loadBlacklistData();
    }

    private void loadBlacklistData() {
        for (BlacklistData blacklistData : factionManager.getBlacklists()) {
            blacklistMap.put(blacklistData.getUuid(), blacklistData);
        }
    }

    public void reloadDisplayNames(Player player) {
        clearPlayerScoreboard(player);
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            return;
        }

        String scoreboardName = "faction_display";
        Scoreboard scoreboard = scoreboardAPI.getScoreboard(player, scoreboardName);
        if (scoreboard == null) {
            scoreboardAPI.createScoreboard(player, scoreboardName, "Faction Display");
            scoreboard = scoreboardAPI.getScoreboard(player, scoreboardName);
        }

        FactionData playerFactionData = factionManager.getFactionData(playerData.getFaction());
        if (playerFactionData == null) {
            return;
        }

        // Cache commonly used values to avoid redundant method calls
        String playerFaction = playerData.getFaction();
        boolean hasBlacklist = playerFactionData.hasBlacklist();

        for (PlayerData p : playerManager.getPlayers()) {
            Player pPlayer = p.getPlayer();
            String pFaction = p.getFaction();
            String teamName = pPlayer.getName();

            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
            }

            if (pFaction != null) {
                if (pFaction.equalsIgnoreCase(playerFaction)) {
                    String colorCode = ChatColor.translateAlternateColorCodes('&', "&" + playerFactionData.getPrimaryColor());
                    team.setPrefix(colorCode);
                    team.setColor(ChatColor.getByChar(playerFactionData.getPrimaryColor().charAt(0)));
                } else if (factionManager.isInBündnisWith(pPlayer, playerFaction)) {
                    FactionData fData = factionManager.getFactionData(pFaction);
                    String colorCode = ChatColor.translateAlternateColorCodes('&', "&" + fData.getPrimaryColor());
                    team.setPrefix(colorCode);
                    team.setColor(ChatColor.getByChar(fData.getPrimaryColor().charAt(0)));
                }
            } else if (playerFaction.equalsIgnoreCase("ICA") && ServerManager.contractDataMap.get(pPlayer.getUniqueId().toString()) != null) {
                team.setPrefix(ChatColor.RED.toString());
                team.setColor(ChatColor.RED);
            } else if (hasBlacklist) {
                BlacklistData blacklistData = blacklistMap.get(pPlayer.getUniqueId().toString());
                if (blacklistData != null && blacklistData.getFaction().equalsIgnoreCase(playerFactionData.getName())) {
                    team.setPrefix(ChatColor.RED.toString());
                    team.setColor(ChatColor.RED);
                }
            }

            team.addEntry(pPlayer.getName());
        }

        scoreboardAPI.updateScoreboardTitle(player, scoreboardName, "Faction Display");
    }

    public void clearPlayerScoreboard(Player player) {
        playerScoreboards.remove(player);
        scoreboardAPI.removeScoreboard(player, "faction_display");
    }
}
