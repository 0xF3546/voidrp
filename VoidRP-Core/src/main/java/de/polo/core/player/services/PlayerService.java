package de.polo.core.player.services;

import de.polo.core.game.base.extra.PlaytimeReward;
import de.polo.api.player.VoidPlayer;
import de.polo.api.jobs.enums.LongTermJob;
import de.polo.core.storage.LoyaltyBonusTimer;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.enums.EXPType;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PlayerService {
    boolean isCreated(UUID uuid);
    void updatePlayer(String uuid, String name, String adress);
    void loadPlayer(Player player);
    void savePlayer(Player player) throws Exception;
    PlaytimeReward getPlaytimeReward(int id);
    LoyaltyBonusTimer getLoyaltyTimer(UUID uuid);
    void add1MinutePlaytime(Player player) throws Exception;
    void addMoney(Player player, int amount, String reason) throws Exception;
    void removeMoney(Player player, int amount, String reason) throws Exception;
    void addBankMoney(Player player, int amount, String reason) throws Exception;
    void removeBankMoney(Player player, int amount, String reason) throws Exception;
    void setRang(UUID uuid, String rank);
    int money(Player player);
    int bank(Player player);
    String firstname(Player player);
    String lastname(Player player);
    int visum(Player player);
    int paydayDuration(Player player);
    void setPlayerMove(Player player, Boolean state);
    boolean canPlayerMove(Player player);
    boolean isTeam(Player player);
    Integer perms(Player player);
    String rang(Player player);
    void startTimeTracker();
    void kickPlayer(Player player, String reason);
    void clearExpBoost(Player player) throws Exception;
    void addExp(Player player, Integer exp);
    void addExp(Player player, EXPType expType, Integer amount);
    void addEXPBoost(Player player, int hours) throws Exception;
    void redeemRank(Player player, String type, int duration, String duration_type) throws Exception;
    boolean isInStaatsFrak(Player player);
    void openInterActionMenu(Player player, Player targetplayer);
    void openFactionInteractionMenu(Player player, Player targetplayer, String faction);
    PlayerData getPlayerData(UUID uuid);
    PlayerData getPlayerData(Player player);
    Collection<PlayerData> getPlayers();
    void addCoins(Player player, int amount);
    void removeCoins(Player player, int amount);
    void setPlayerSpawn(PlayerData playerData, String spawn) throws Exception;
    void carryPlayer(Player player, Player target);
    void removeTargetFromArmorStand(Player player);
    boolean isCarrying(org.bukkit.entity.Entity entity);
    int getGeworbenCount(Player player) throws Exception;
    void kissPlayer(Player player, Player targetplayer);
    void setLongTermJob(VoidPlayer player, LongTermJob longTermJob);
}
