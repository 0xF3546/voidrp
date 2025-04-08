package de.polo.core.faction.service.impl;

import de.polo.core.faction.service.FactionService;
import de.polo.core.game.faction.SprayableBanner;
import de.polo.core.game.faction.staat.SubTeam;
import de.polo.core.faction.entity.Faction;
import de.polo.core.faction.entity.FactionPlayerData;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RegisteredBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

import static de.polo.core.Main.factionManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreFactionService implements FactionService {
    public CoreFactionService() {
    }

    @Override
    public String getPlayerFaction(Player player) {
        return factionManager.faction(player);
    }

    @Override
    public Integer getPlayerFactionGrade(Player player) {
        return factionManager.faction_grade(player);
    }

    @Override
    public void assignPlayerToFaction(Player player, String faction, Integer rank) throws Exception {
        factionManager.setPlayerInFrak(player, faction, rank);
    }

    @Override
    public void removePlayerFromFaction(Player player) throws Exception {
        factionManager.removePlayerFromFrak(player);
    }

    @Override
    public void removeOfflinePlayerFromFaction(OfflinePlayer player) throws Exception {
        factionManager.removeOfflinePlayerFromFrak(player);
    }

    @Override
    public String getFactionFullName(String faction) {
        return factionManager.getFactionFullname(faction);
    }

    @Override
    public Collection<FactionPlayerData> getFactionMembers(String faction) throws Exception {
        return factionManager.getFactionMember(faction);
    }

    @Override
    public boolean isPlayerInGoodFaction(Player player) {
        return factionManager.isPlayerInGoodFaction(player);
    }

    @Override
    public Integer getFactionBank(String faction) {
        return factionManager.factionBank(faction);
    }

    @Override
    public void addFactionMoney(String faction, Integer amount, String reason) throws Exception {
        factionManager.addFactionMoney(faction, amount, reason);
    }

    @Override
    public boolean removeFactionMoney(String faction, Integer amount, String reason) throws Exception {
        return factionManager.removeFactionMoney(faction, amount, reason);
    }

    @Override
    public void sendMessageToFaction(String faction, String message) {
        factionManager.sendMessageToFaction(faction, message);
    }

    @Override
    public void sendCustomMessageToFactions(String message, String... factions) {
        factionManager.sendCustomMessageToFactions(message, factions);
    }

    @Override
    public void sendCustomLeaderMessageToFactions(String message, String... factions) {
        factionManager.sendCustomLeaderMessageToFactions(message, factions);
    }

    @Override
    public void sendCustomMessageToFaction(String faction, String message) {
        factionManager.sendCustomMessageToFaction(faction, message);
    }

    @Override
    public boolean changeRankPayDay(String faction, int rank, int payday) throws Exception {
        return factionManager.changeRankPayDay(faction, rank, payday);
    }

    @Override
    public boolean changeRankName(String faction, int rank, String name) throws Exception {
        return factionManager.changeRankName(faction, rank, name);
    }

    @Override
    public String getTitle(Player player) {
        return factionManager.getTitle(player);
    }

    @Override
    public void setDuty(Player player, boolean state) {
        factionManager.setDuty(player, state);
    }

    @Override
    public boolean isInAlliance(Player player) {
        return factionManager.isInBündnis(player);
    }

    @Override
    public boolean isInAllianceWith(Player player, String faction) {
        return factionManager.isInBündnisWith(player, faction);
    }

    @Override
    public int getMemberCount(String faction) {
        return factionManager.getMemberCount(faction);
    }

    @Override
    public int getOnlineMemberCount(String faction) {
        return factionManager.getOnlineMemberCount(faction);
    }

    @Override
    public Faction getFactionData(int factionId) {
        return factionManager.getFactionData(factionId);
    }

    @Override
    public Faction getFactionData(String faction) {
        return factionManager.getFactionData(faction);
    }

    @Override
    public boolean isFactionMemberInRange(String faction, Location location, int range, boolean ignoreDeath) {
        return factionManager.isFactionMemberInRange(faction, location, range, ignoreDeath);
    }

    @Override
    public Collection<PlayerData> getFactionMembersInRange(String faction, Location location, int range, boolean ignoreDeath) {
        return factionManager.getFactionMemberInRange(faction, location, range, ignoreDeath);
    }

    @Override
    public PlayerData getFactionOfPlayer(UUID uuid) throws Exception {
        return factionManager.getFactionOfPlayer(uuid);
    }

    @Override
    public void createSubTeam(SubTeam subTeam) throws Exception {
        factionManager.createSubTeam(subTeam);
    }

    @Override
    public void deleteSubTeam(SubTeam subTeam) throws Exception {
        factionManager.deleteSubTeam(subTeam);
    }

    @Override
    public Collection<SubTeam> getSubTeams(int factionId) {
        return factionManager.getSubTeams(factionId);
    }

    @Override
    public void setFactionMOTD(int factionId, String motd) throws Exception {
        factionManager.setFactionMOTD(factionId, motd);
    }

    @Override
    public void setFactionChatColor(int factionId, ChatColor color) throws Exception {
        factionManager.setFactionChatColor(factionId, color);
    }

    @Override
    public void updateBanner(RegisteredBlock block, Faction faction) throws Exception {
        factionManager.updateBanner(block, faction);
    }

    @Override
    public boolean canSprayBanner(RegisteredBlock block) {
        return factionManager.canSprayBanner(block);
    }

    @Override
    public boolean isBannerRegistered(RegisteredBlock block) {
        return factionManager.isBannerRegistered(block);
    }

    @Override
    public Collection<SprayableBanner> getBanners() {
        return factionManager.getBanner();
    }

    @Override
    public void setLeader(OfflinePlayer offlinePlayer, boolean leader) {
        factionManager.setLeader(offlinePlayer, leader);
    }
}