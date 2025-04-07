package de.polo.voidroleplay.faction.service;

import de.polo.voidroleplay.game.faction.SprayableBanner;
import de.polo.voidroleplay.game.faction.staat.SubTeam;
import de.polo.voidroleplay.faction.entity.Faction;
import de.polo.voidroleplay.faction.entity.FactionPlayerData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.RegisteredBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface FactionService {
    String getPlayerFaction(Player player);
    Integer getPlayerFactionGrade(Player player);
    void assignPlayerToFaction(Player player, String faction, Integer rank) throws Exception;
    void removePlayerFromFaction(Player player) throws Exception;
    void removeOfflinePlayerFromFaction(OfflinePlayer player) throws Exception;
    String getFactionFullName(String faction);
    Collection<FactionPlayerData> getFactionMembers(String faction) throws Exception;
    boolean isPlayerInGoodFaction(Player player);
    Integer getFactionBank(String faction);
    void addFactionMoney(String faction, Integer amount, String reason) throws Exception;
    boolean removeFactionMoney(String faction, Integer amount, String reason) throws Exception;
    void sendMessageToFaction(String faction, String message);

    // Neue Methoden
    void sendCustomMessageToFactions(String message, String... factions);
    void sendCustomLeaderMessageToFactions(String message, String... factions);
    void sendCustomMessageToFaction(String faction, String message);
    boolean changeRankPayDay(String faction, int rank, int payday) throws Exception;
    boolean changeRankName(String faction, int rank, String name) throws Exception;
    String getTitle(Player player);
    void setDuty(Player player, boolean state);
    boolean isInAlliance(Player player);
    boolean isInAllianceWith(Player player, String faction);
    int getMemberCount(String faction);
    int getOnlineMemberCount(String faction);
    Faction getFactionData(int factionId);
    Faction getFactionData(String faction);
    boolean isFactionMemberInRange(String faction, Location location, int range, boolean ignoreDeath);
    Collection<PlayerData> getFactionMembersInRange(String faction, Location location, int range, boolean ignoreDeath);
    PlayerData getFactionOfPlayer(UUID uuid) throws Exception;
    void createSubTeam(SubTeam subTeam) throws Exception;
    void deleteSubTeam(SubTeam subTeam) throws Exception;
    Collection<SubTeam> getSubTeams(int factionId);
    void setFactionMOTD(int factionId, String motd) throws Exception;
    void setFactionChatColor(int factionId, ChatColor color) throws Exception;
    void updateBanner(RegisteredBlock block, Faction faction) throws Exception;
    boolean canSprayBanner(RegisteredBlock block);
    boolean isBannerRegistered(RegisteredBlock block);
    Collection<SprayableBanner> getBanners();
    void setLeader(OfflinePlayer offlinePlayer, boolean leader);
}