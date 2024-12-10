package de.polo.voidroleplay.game.base;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import de.polo.api.nametags.INameTagProvider;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class NameTagProviderImpl implements INameTagProvider {

    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    @Override
    public void setNametag(final Player player, final String prefix, final String suffix) {
        try {
            PacketContainer packet = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.SCOREBOARD_TEAM);

            // Team name (must be unique)
            String teamName = "team_" + player.getUniqueId().toString().substring(0, 8);

            packet.getStrings().write(0, teamName); // Team name
            packet.getStrings().write(1, prefix.replace("&", "§"));  // Prefix
            packet.getStrings().write(2, suffix.replace("&", "§"));  // Suffix

            // Player(s) to add to the team
            packet.getSpecificModifier(Collection.class).write(0, List.of(player.getName()));
            packet.getIntegers().write(1, 0); // Mode: 0 = Create Team

            // Send the packet to all online players
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                protocolManager.sendServerPacket(onlinePlayer, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setNametagForViewers(final Player player, final Collection<Player> viewers, final String prefix, final String suffix) {
        try {
            PacketContainer packet = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.SCOREBOARD_TEAM);

            String teamName = "team_" + player.getUniqueId().toString().substring(0, 8);

            packet.getStrings().write(0, teamName);
            packet.getStrings().write(1, prefix.replace("&", "§"));
            packet.getStrings().write(2, suffix.replace("&", "§"));

            packet.getSpecificModifier(Collection.class).write(0, List.of(player.getName()));
            packet.getIntegers().write(1, 0); // Mode: 0 = Create Team

            for (Player viewer : viewers) {
                protocolManager.sendServerPacket(viewer, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearNametag(final Player player) {
        try {
            PacketContainer packet = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.SCOREBOARD_TEAM);

            String teamName = "team_" + player.getUniqueId().toString().substring(0, 8);

            packet.getStrings().write(0, teamName);
            packet.getIntegers().write(1, 1); // Mode: 1 = Remove Team

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                protocolManager.sendServerPacket(onlinePlayer, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearAllNametags() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            clearNametag(player);
        }
    }

    @Override
    public void setTabHeaderFooter(final Player player, final String header, final String footer) {
        try {
            PacketContainer packet = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);

            WrappedChatComponent headerComponent = WrappedChatComponent.fromText(header);
            WrappedChatComponent footerComponent = WrappedChatComponent.fromText(footer);

            packet.getChatComponents().write(0, headerComponent); // Header
            packet.getChatComponents().write(1, footerComponent); // Footer

            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTabForAll(final String header, final String footer) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            setTabHeaderFooter(player, header, footer);
        }
    }

    public void updateForFaction(String faction) {
        FactionData factionData = Main.getInstance().factionManager.getFactionData(faction);
        if (factionData == null) return;
        List<Player> viewers = new ObjectArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player);
            if (playerData == null) continue;
            if (playerData.getFaction() == null) continue;
            if (!playerData.getFaction().equalsIgnoreCase(faction)) continue;
            clearNametag(player);
            viewers.add(player);
        }
        for (Player player : viewers) {
            setNametagForViewers(player, viewers, "§" + factionData.getPrimaryColor(), "");
        }
    }
}
