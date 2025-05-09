package de.polo.core.utils.gameplay;

import de.polo.core.storage.BlacklistData;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.player.ScoreboardAPI;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DisplayNameManager {

    private final Map<Player, String> playerScoreboards = new HashMap<>();
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Map<String, BlacklistData> blacklistMap = new HashMap<>();
    private final ScoreboardAPI scoreboardAPI;
    private final FactionDisplayNameManager factionDisplayNameManager;

    public DisplayNameManager(PlayerManager playerManager, FactionManager factionManager, ScoreboardAPI scoreboardAPI) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.scoreboardAPI = scoreboardAPI;
        this.factionDisplayNameManager = new FactionDisplayNameManager();
        loadBlacklistData();
    }

    private void loadBlacklistData() {
        for (BlacklistData blacklistData : factionManager.getBlacklists()) {
            blacklistMap.put(blacklistData.getUuid(), blacklistData);
        }
    }

    public void reloadDisplayNames(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
        }
        //factionDisplayNameManager.updateForFaction(playerData.getFaction());
        /*clearPlayerScoreboard(player);
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

        scoreboardAPI.updateScoreboardTitle(player, scoreboardName, "Faction Display");*/
    }

    public void clearPlayerScoreboard(Player player) {
        playerScoreboards.remove(player);
        scoreboardAPI.removeScoreboard(player, "faction_display");
    }
}
