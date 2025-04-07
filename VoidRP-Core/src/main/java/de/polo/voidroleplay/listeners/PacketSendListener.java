package de.polo.voidroleplay.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.storage.BlacklistData;
import de.polo.voidroleplay.faction.entity.Faction;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.WantedReason;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.Objects;
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
            if (sender == null) continue;

            PlayerData targetData = playerManager.getPlayerData(target.getUniqueId());
            PlayerData senderData = playerManager.getPlayerData(sender.getUniqueId());
            if (targetData == null || senderData == null) continue;

            Faction factionData = factionManager.getFactionData(senderData.getFaction());

            entry.setDisplayName(null);

            if(factionData != null) {
                processGoodFaction(target, sender, entry, targetData, senderData);
                processBadFaction(entry, senderData, targetData, sender);
                processSameFaction(entry, sender, targetData, factionData);
            }

            processRelationship(entry, sender, targetData, senderData);
            processNoneNameTag(entry, sender, senderData);
            processGameMode(entry, senderData, sender);
            processReport(entry, sender);
            processAFK(entry, sender, senderData);
        }
    }

    private void processGoodFaction(Player target, Player sender, WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, PlayerData targetData, PlayerData senderData) {
        if (factionManager.isPlayerInGoodFaction(target) && !"Medic".equalsIgnoreCase(targetData.getFaction())) {
            if (senderData.getWanted() != null) {
                WantedReason wantedReason = utils.staatUtil.getWantedReason(senderData.getWanted().getWantedId());
                if (wantedReason != null) {
                    int wantedLevel = wantedReason.getWanted();
                    NamedTextColor color = wantedLevel >= 50 ? NamedTextColor.DARK_RED : NamedTextColor.RED;
                    entry.setDisplayName(Component.text(MessageFormat.format("{0}", sender.getName())).color(color));
                    //entry.getGameProfile().setName(MessageFormat.format(net.md_5.bungee.api.ChatColor.of(color.asHexString()) + "{0} {1}", sender.getName(), wantedLevel));
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
            //entry.getGameProfile().setName(ChatColor.DARK_RED + sender.getName());
        }
    }

    private void processSameFaction(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, Player sender, PlayerData targetData, Faction factionData) {
        if (!targetData.getFaction().equalsIgnoreCase(factionData.getName())) return;
        entry.setDisplayName(Component.text("§" + factionData.getPrimaryColor() + sender.getName()));
        //entry.getGameProfile().setName("§" + factionData.getPrimaryColor() + sender.getName());
    }

    private void processRelationship(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, Player sender, PlayerData targetData, PlayerData senderData) {
        if (targetData.getRelationShip().isEmpty() || senderData.getRelationShip().isEmpty()) return;
        if (!targetData.getRelationShip().containsKey(sender.getUniqueId().toString())) return;
        entry.setDisplayName(Component.text(sender.getName()).color(NamedTextColor.LIGHT_PURPLE));
        //entry.getGameProfile().setName(ChatColor.LIGHT_PURPLE + sender.getName());
    }

    private void processGameMode(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, PlayerData senderData, Player sender){
        if (sender.getGameMode() == GameMode.CREATIVE) {
            if (entry.getDisplayName() != null) {
                entry.setDisplayName(Component.text(MessageFormat.format("{0}[{1}GM{0}] {2}",ChatColor.DARK_GRAY, ChatColor.DARK_GREEN, ChatColor.RESET )).append(entry.getDisplayName()));
                //entry.getGameProfile().setName(MessageFormat.format("{0}[{1}GM{0}]{2} {3}",ChatColor.DARK_GRAY, ChatColor.DARK_GREEN, ChatColor.RESET, entry.getDisplayName().toString()));
            }
            else{
                entry.setDisplayName(Component.text(MessageFormat.format("{0}[{1}GM{0}]{2} {3}",ChatColor.DARK_GRAY, ChatColor.DARK_GREEN, ChatColor.GRAY, sender.getName())));
                //entry.getGameProfile().setName(MessageFormat.format("{0}[{1}GM{0}]{2} {3}",ChatColor.DARK_GRAY, ChatColor.DARK_GREEN, ChatColor.RESET, sender.getName()));
            }
        }
    }

    private  void processAFK(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, Player sender, PlayerData senderData){
        if(senderData.isAFK()){
            if (entry.getDisplayName() != null) {
                entry.setDisplayName(Component.text(MessageFormat.format("{0}[{1}AFK{0}] {2}",ChatColor.DARK_GRAY, ChatColor.DARK_PURPLE, ChatColor.RESET)).append(entry.getDisplayName()));
                //entry.getGameProfile().setName(MessageFormat.format("{0}[{1}AFK{0}]{2} {3}",ChatColor.DARK_GRAY, ChatColor.DARK_PURPLE, ChatColor.RESET, entry.getDisplayName().toString()));
            }
            else{
                entry.setDisplayName(Component.text(MessageFormat.format("{0}[{1}AFK{0}]{2} {3}",ChatColor.DARK_GRAY, ChatColor.DARK_PURPLE, ChatColor.GRAY, sender.getName())));
                //entry.getGameProfile().setName(MessageFormat.format("{0}[{1}AFK{0}]{2} {3}",ChatColor.DARK_GRAY, ChatColor.DARK_PURPLE, ChatColor.RESET, sender.getName()));
            }
        }
    }

    private void processReport(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, Player sender){
        if(Main.supportManager.isInAcceptedTicket(sender)){
            if (entry.getDisplayName() != null) {
                entry.setDisplayName(Component.text(MessageFormat.format("{0}[{1}R{0}] {2}",ChatColor.DARK_GRAY, ChatColor.GOLD, ChatColor.RESET)).append(entry.getDisplayName()));
                //entry.getGameProfile().setName(MessageFormat.format("{0}[{1}R{0}]{2} {3}",ChatColor.DARK_GRAY, ChatColor.GOLD, ChatColor.RESET, entry.getDisplayName().toString()));
            }
            else{
                entry.setDisplayName(Component.text(MessageFormat.format("{0}[{1}R{0}]{2} {3}",ChatColor.DARK_GRAY, ChatColor.GOLD, ChatColor.GRAY, sender.getName())));
                //entry.getGameProfile().setName(MessageFormat.format("{0}[{1}R{0}]{2} {3}",ChatColor.DARK_GRAY, ChatColor.GOLD, ChatColor.RESET, sender.getName()));
            }
        }
    }

    public void processNoneNameTag(WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry, Player sender, PlayerData senderData){
        if(entry.getDisplayName() == null){
            if(Objects.requireNonNull(senderData).isDuty()){
                if (senderData.getFaction().equalsIgnoreCase("Polizei")){
                    entry.setDisplayName(Component.text(sender.getName()).color(NamedTextColor.BLUE));
                }
                else if(senderData.getFaction().equalsIgnoreCase("FBI")){
                    entry.setDisplayName(Component.text(sender.getName()).color(NamedTextColor.DARK_BLUE));
                }
                else if(senderData.getFaction().equalsIgnoreCase("Medic")){
                    entry.setDisplayName(Component.text(sender.getName()).color(NamedTextColor.DARK_RED));
                }
            }
            else{
                entry.setDisplayName(Component.text(sender.getName()).color(NamedTextColor.GRAY));
            }
        }
    }

}
