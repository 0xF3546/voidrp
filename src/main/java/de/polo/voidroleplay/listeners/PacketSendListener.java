package de.polo.voidroleplay.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.storage.BlacklistData;
import de.polo.voidroleplay.storage.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.WantedReason;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.UUID;

import static de.polo.voidroleplay.Main.utils;

public class PacketSendListener implements PacketListener {

    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public PacketSendListener(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.PLAYER_INFO_UPDATE) {
            return;
        }

        Player target = event.getPlayer();
        if (target == null) return;
        WrapperPlayServerPlayerInfoUpdate playerInfoUpdate = new WrapperPlayServerPlayerInfoUpdate(event);

        for (var entry : playerInfoUpdate.getEntries()) {
            Player sender = Bukkit.getPlayer(entry.getProfileId());
            if (sender == null || target.equals(sender)) continue;

            PlayerData targetData = playerManager.getPlayerData(target.getUniqueId());
            PlayerData senderData = playerManager.getPlayerData(sender.getUniqueId());
            if (targetData == null || senderData == null) continue;
            processRelationship(entry, sender, targetData, senderData);

            FactionData factionData = factionManager.getFactionData(senderData.getFaction());
            if (factionData == null) continue;

            processGoodFaction(target, sender, entry, targetData, senderData);
            processBadFaction(entry, senderData, targetData, sender);
            processSameFaction(entry, sender, targetData, factionData);

            processGameMode(entry, senderData, sender);
        }
    }

    private void processGoodFaction(Player target, Player sender, WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, PlayerData targetData, PlayerData senderData) {
        if (factionManager.isPlayerInGoodFaction(target) && !"Medic".equalsIgnoreCase(targetData.getFaction())) {
            if (senderData.getWanted() != null) {
                WantedReason wantedReason = utils.staatUtil.getWantedReason(senderData.getWanted().getWantedId());
                if (wantedReason != null) {
                    int wantedLevel = wantedReason.getWanted();
                    NamedTextColor color = wantedLevel >= 50 ? NamedTextColor.DARK_RED : NamedTextColor.RED;
                    entry.setDisplayName(Component.text(MessageFormat.format("{0} {1} WPS", sender.getName(), wantedLevel)).color(color));
                }
            }
        }
    }

    private void processBadFaction(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, PlayerData senderData, PlayerData targetData, Player sender) {
        for (BlacklistData blacklistData : factionManager.getBlacklists()) {
            if (blacklistData == null) continue;
            if (UUID.fromString(blacklistData.getUuid()) != senderData.getUuid()
                    || !blacklistData.getFaction().equalsIgnoreCase(targetData.getFaction())) continue;
            entry.setDisplayName(Component.text(sender.getName()).color(NamedTextColor.DARK_RED));
        }
    }

    private void processSameFaction(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, Player sender, PlayerData targetData, FactionData factionData) {
        if (!targetData.getFaction().equalsIgnoreCase(factionData.getName())) return;
        entry.setDisplayName(Component.text("§" + factionData.getPrimaryColor() + sender.getName()));
    }

    private void processRelationship(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, Player sender, PlayerData targetData, PlayerData senderData) {
        if (targetData.getRelationShip().isEmpty() || senderData.getRelationShip().isEmpty()) return;
        if (!targetData.getRelationShip().containsKey(sender.getUniqueId().toString())) return;
        entry.setDisplayName(Component.text(sender.getName()).color(NamedTextColor.LIGHT_PURPLE));
    }

    private void processGameMode(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, PlayerData senderData, Player sender){
        if (sender.getGameMode() == GameMode.CREATIVE) {
            if (entry.getDisplayName() != null) {
                entry.setDisplayName(Component.text(MessageFormat.format("[{0}GM{1}]", ChatColor.DARK_GREEN, ChatColor.RESET)).append(entry.getDisplayName()));
            }
            else{

                //entry.setDisplayName(Component.text(MessageFormat.format("[{0}GM{1}] {2}", ChatColor.DARK_GREEN, ChatColor.RESET, sender.getName())));
            }
        }
    }
}
