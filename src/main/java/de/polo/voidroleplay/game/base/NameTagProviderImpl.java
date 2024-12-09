package de.polo.voidroleplay.game.base;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import de.polo.api.nametags.INameTagProvider;
import de.polo.voidroleplay.utils.ColorTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
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
        if (player == null || viewers == null || name == null || prefix == null || suffix == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }

        if (!viewers.iterator().hasNext()) {
            return false;
        }

        try {
            String teamName = "team_" + player.getUniqueId().toString().substring(0, 8);

            PacketContainer teamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
            teamPacket.getStrings().write(0, teamName);
            teamPacket.getIntegers().write(0, 0); // Action: Create team
            teamPacket.getChatComponents()
                    .write(0, WrappedChatComponent.fromText(prefix.replace("&", "§"))) // Prefix
                    .write(1, WrappedChatComponent.fromText(suffix.replace("&", "§"))); // Suffix

            PacketContainer addPlayersPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
            addPlayersPacket.getStrings().write(0, teamName);
            addPlayersPacket.getIntegers().write(0, 3); // Action: Add players
            addPlayersPacket.getSpecificModifier(Collection.class)
                    .write(0, Collections.singletonList(player.getName()));

            for (Player viewer : viewers) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, teamPacket);
                ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, addPlayersPacket);
            }

            return true;
        } catch (Exception e) {
            System.err.println("Failed to set nametag for group: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean clearNametagForGroup(final Player player, final Iterable<Player> viewers) {
        if (player == null || viewers == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }

        if (!viewers.iterator().hasNext()) {
            return false;
        }

        try {
            String teamName = "team_" + player.getUniqueId().toString().substring(0, 8);

            PacketContainer removePlayersPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
            removePlayersPacket.getStrings().write(0, teamName);
            removePlayersPacket.getIntegers().write(0, 4); // Action: Remove players
            removePlayersPacket.getSpecificModifier(Collection.class)
                    .write(0, Collections.singletonList(player.getName()));

            for (Player viewer : viewers) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, removePlayersPacket);
            }

            return true;
        } catch (Exception e) {
            System.err.println("Failed to clear nametag for group: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
