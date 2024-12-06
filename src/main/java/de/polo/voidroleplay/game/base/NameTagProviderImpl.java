package de.polo.voidroleplay.game.base;

import de.polo.api.nametags.INameTagProvider;
import de.polo.voidroleplay.utils.ColorTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

public class NameTagProviderImpl implements INameTagProvider {
    private final Scoreboard scoreboard;
    private final Map<String, Team> customTeams;

    public NameTagProviderImpl() {
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        this.customTeams = new HashMap<>();
    }

    public NameTagProviderImpl(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
        this.customTeams = new HashMap<>();
    }

    @Override
    public boolean setNametag(Player player, String name, String prefix, String suffix) {
        Team team = this.customTeams.get(name);
        if (team == null) {
            team = this.scoreboard.registerNewTeam(name);
            this.customTeams.put(name, team);
        }
        team.prefix(ColorTranslator.translateColorCodes(prefix));
        team.suffix(ColorTranslator.translateColorCodes(suffix));
        team.addEntry(player.getName());
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        return true;
    }

    @Override
    public boolean clearNametag(Player player) {
        for (Team team : this.customTeams.values()) {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }
        return true;
    }

    @Override
    public boolean clearAll() {
        for (Team team : this.customTeams.values()) {
            team.unregister();
        }
        this.customTeams.clear();
        return true;
    }

    @Override
    public boolean setNametagForGroup(final Player player, final Iterable<Player> viewers, final String name, final String prefix, final String suffix) {
        Team team = this.customTeams.get(name);
        if (team == null) {
            team = this.scoreboard.registerNewTeam(name);
            this.customTeams.put(name, team);
        }
        team.prefix(ColorTranslator.translateColorCodes(prefix));
        team.suffix(ColorTranslator.translateColorCodes(suffix));
        team.addEntry(player.getName());
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        for (Player viewer : viewers) {
            viewer.setScoreboard(this.scoreboard);
        }
        return true;
    }
}
