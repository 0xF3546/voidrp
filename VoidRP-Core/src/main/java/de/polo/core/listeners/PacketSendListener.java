package de.polo.core.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import de.polo.api.Utils.ApiUtils;
import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.faction.service.LawEnforcementService;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.storage.BlacklistData;
import de.polo.core.storage.WantedReason;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class PacketSendListener implements PacketListener {

    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public PacketSendListener(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.PLAYER_INFO_UPDATE) return;

        Player target = event.getPlayer();
        if (target == null) return;

        WrapperPlayServerPlayerInfoUpdate playerInfoUpdate = new WrapperPlayServerPlayerInfoUpdate(event);

        for (var entry : playerInfoUpdate.getEntries()) {
            Player sender = Bukkit.getPlayer(entry.getProfileId());
            if (sender == null) continue;

            PlayerData targetData = playerManager.getPlayerData(target.getUniqueId());
            PlayerData senderData = playerManager.getPlayerData(sender.getUniqueId());
            if (targetData == null || senderData == null) continue;

            entry.setDisplayName(null);

            if (senderData.getFaction() != null) {
                Faction factionData = factionManager.getFactionData(senderData.getFaction());
                processGoodFaction(target, sender, entry, targetData, senderData);
                processBadFaction(entry, senderData, targetData, sender);
                processSameFaction(entry, sender, targetData, factionData);
            }

            processRelationship(entry, sender, targetData, senderData);
            processNoneNameTag(entry, sender, senderData);
            processGameMode(entry, sender);
            processReport(entry, sender);
            processAFK(entry, sender, senderData);
        }
    }

    private void processGoodFaction(Player target, Player sender, WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry,
                                    PlayerData targetData, PlayerData senderData) {
        if (targetData.getFaction() == null) return;

        if (factionManager.isPlayerInGoodFaction(target) && !"Medic".equalsIgnoreCase(targetData.getFaction())) {
            if (senderData.getWanted() != null) {
                LawEnforcementService lawEnforcementService = VoidAPI.getService(LawEnforcementService.class);
                WantedReason reason = lawEnforcementService.getWantedReason(senderData.getWanted().getWantedId());
                if (reason != null) {
                    int level = reason.getWanted();
                    NamedTextColor color = level >= 50 ? NamedTextColor.DARK_RED : NamedTextColor.RED;
                    entry.setDisplayName(Component.text(sender.getName() + " " + level, color));
                }
            }
        }
    }

    private void processBadFaction(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry,
                                   PlayerData senderData, PlayerData targetData, Player sender) {
        for (BlacklistData blacklistData : factionManager.getBlacklists()) {
            if (blacklistData == null) continue;
            if (!UUID.fromString(blacklistData.getUuid()).equals(senderData.getUuid())) continue;
            if (!blacklistData.getFaction().equalsIgnoreCase(targetData.getFaction())) continue;

            entry.setDisplayName(Component.text(sender.getName(), NamedTextColor.DARK_RED));
        }
    }

    private void processSameFaction(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry,
                                    Player sender, PlayerData targetData, Faction factionData) {
        if (!targetData.getFaction().equalsIgnoreCase(factionData.getName())) return;

        NamedTextColor color = ApiUtils.getColorFromCode(factionData.getPrimaryColor());
        entry.setDisplayName(Component.text(sender.getName(), color));
    }

    private void processRelationship(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry,
                                     Player sender, PlayerData targetData, PlayerData senderData) {
        if (targetData.getRelationShip().isEmpty() || senderData.getRelationShip().isEmpty()) return;
        if (!targetData.getRelationShip().containsKey(sender.getUniqueId().toString())) return;

        entry.setDisplayName(Component.text(sender.getName(), NamedTextColor.LIGHT_PURPLE));
    }

    private void processGameMode(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, Player sender) {
        if (sender.getGameMode() != GameMode.CREATIVE) return;

        Component prefix = Component.text("[GM] ", NamedTextColor.DARK_GREEN);
        Component name = entry.getDisplayName() != null
                ? entry.getDisplayName()
                : Component.text(sender.getName(), NamedTextColor.GRAY);

        entry.setDisplayName(prefix.append(name));
    }

    private void processAFK(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, Player sender, PlayerData senderData) {
        if (!senderData.isAFK()) return;

        Component prefix = Component.text("[AFK] ", NamedTextColor.DARK_PURPLE);
        Component name = entry.getDisplayName() != null
                ? entry.getDisplayName()
                : Component.text(sender.getName(), NamedTextColor.GRAY);

        entry.setDisplayName(prefix.append(name));
    }

    private void processReport(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, Player sender) {
        if (!Main.supportManager.isInAcceptedTicket(sender)) return;

        Component prefix = Component.text("[R] ", NamedTextColor.GOLD);
        Component name = entry.getDisplayName() != null
                ? entry.getDisplayName()
                : Component.text(sender.getName(), NamedTextColor.GRAY);

        entry.setDisplayName(prefix.append(name));
    }

    private void processNoneNameTag(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry,
                                    Player sender, PlayerData senderData) {
        if (entry.getDisplayName() != null) return;

        NamedTextColor color = NamedTextColor.GRAY;

        if (senderData.isDuty() && senderData.getFaction() != null) {
            switch (senderData.getFaction().toLowerCase()) {
                case "polizei" -> color = NamedTextColor.BLUE;
                case "fbi" -> color = NamedTextColor.DARK_BLUE;
                case "medic" -> color = NamedTextColor.DARK_RED;
            }
        }

        entry.setDisplayName(Component.text(sender.getName(), color));
    }
}