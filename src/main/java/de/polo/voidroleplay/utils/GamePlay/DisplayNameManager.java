package de.polo.voidroleplay.utils.GamePlay;

import de.polo.voidroleplay.dataStorage.BlacklistData;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

/**
 * Verwaltet die Anzeige von Spielernamen in der Tab-Liste und über dem Kopf der Spieler.
 * @version 1.0.1
 * @since 1.0.0
 */
public class DisplayNameManager {

    private final Map<Player, Scoreboard> playerScoreboards = new HashMap<>();

    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public DisplayNameManager(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
    }

    public void reloadDisplayNames(Player player) {
        clearPlayerScoreboard(player);
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() != null) {
            Scoreboard scoreboard = playerScoreboards.computeIfAbsent(player, k -> Bukkit.getScoreboardManager().getNewScoreboard());

            for (PlayerData p : playerManager.getPlayers()) {
                if (p.getFaction() == null) continue;
                FactionData factionData = factionManager.getFactionData(playerData.getFaction());
                Team team = scoreboard.getTeam(p.getPlayer().getName());
                if (team == null) {
                    team = scoreboard.registerNewTeam(p.getPlayer().getName());
                }

                if (p.getFaction().equalsIgnoreCase(playerData.getFaction())) {
                    team.setPrefix("§" + factionData.getPrimaryColor());
                } else if (playerData.getFaction().equalsIgnoreCase("ICA") && ServerManager.contractDataMap.get(p.getPlayer().getUniqueId().toString()) != null) {
                    team.setPrefix("§c");
                } else if (factionData.hasBlacklist()) {
                    for (BlacklistData blacklistData : factionManager.getBlacklists()) {
                        if (blacklistData.getFaction().equalsIgnoreCase(factionData.getName()) &&
                                blacklistData.getUuid().equalsIgnoreCase(p.getPlayer().getUniqueId().toString())) {
                            team.setPrefix("§c");
                        }
                    }
                }

                team.addEntry(p.getPlayer().getName());
            }

            player.setScoreboard(scoreboard);
        }
    }

    public void clearPlayerScoreboard(Player player) {
        playerScoreboards.remove(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
