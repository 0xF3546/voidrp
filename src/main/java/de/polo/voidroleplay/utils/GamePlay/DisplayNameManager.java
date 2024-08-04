package de.polo.voidroleplay.utils.GamePlay;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.BlacklistData;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.ServerManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.polo.voidroleplay.utils.playerUtils.ScoreboardAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

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
        if (playerData.getFaction() != null) {

            String scoreboardName = "faction_display";
            Scoreboard scoreboard = scoreboardAPI.getScoreboard(player, scoreboardName);
            if (scoreboard == null) {
                scoreboardAPI.createScoreboard(player, scoreboardName, "Faction Display");
                scoreboard = scoreboardAPI.getScoreboard(player, scoreboardName);
            }

            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            if (factionData == null) {
                return;
            }

            for (PlayerData p : playerManager.getPlayers()) {
                if (p.getFaction() == null) continue;

                String teamName = p.getPlayer().getName();
                Team team = scoreboard.getTeam(teamName);
                if (team == null) {
                    team = scoreboard.registerNewTeam(teamName);
                }

                if (p.getFaction().equalsIgnoreCase(playerData.getFaction())) {
                    String colorCode = ChatColor.translateAlternateColorCodes('&', "&" + factionData.getPrimaryColor());
                    team.setPrefix(colorCode);
                    team.setColor(ChatColor.getByChar(factionData.getPrimaryColor().charAt(0)));
                } else if (playerData.getFaction().equalsIgnoreCase("ICA") && ServerManager.contractDataMap.get(p.getPlayer().getUniqueId().toString()) != null) {
                    team.setPrefix(ChatColor.RED.toString());
                    team.setColor(ChatColor.RED);
                } else if (factionData.hasBlacklist()) {
                    BlacklistData blacklistData = blacklistMap.get(p.getPlayer().getUniqueId().toString());
                    if (blacklistData != null && blacklistData.getFaction().equalsIgnoreCase(factionData.getName())) {
                        team.setPrefix(ChatColor.RED.toString());
                        team.setColor(ChatColor.RED);
                    }
                }

                team.addEntry(p.getPlayer().getName());
            }

            scoreboardAPI.updateScoreboardTitle(player, scoreboardName, "Faction Display");
        }
    }

    public void clearPlayerScoreboard(Player player) {
        playerScoreboards.remove(player);
        scoreboardAPI.removeScoreboard(player, "faction_display");
    }
}