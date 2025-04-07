package de.polo.voidroleplay.player.services.impl;

import de.polo.voidroleplay.game.base.extra.PlaytimeReward;
import de.polo.voidroleplay.player.enums.LongTermJob;
import de.polo.voidroleplay.player.services.PlayerService;
import de.polo.voidroleplay.storage.LoyaltyBonusTimer;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.enums.EXPType;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

import static de.polo.voidroleplay.Main.playerManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CorePlayerService implements PlayerService {

    @Override
    public boolean isCreated(UUID uuid) {
        return playerManager.isCreated(uuid);
    }

    @Override
    public void updatePlayer(String uuid, String name, String adress) {
        playerManager.updatePlayer(uuid, name, adress);
    }

    @Override
    public void loadPlayer(Player player) {
        playerManager.loadPlayer(player);
    }

    @Override
    public void savePlayer(Player player) throws Exception {
        playerManager.savePlayer(player);
    }

    @Override
    public PlaytimeReward getPlaytimeReward(int id) {
        return playerManager.getPlaytimeReward(id);
    }

    @Override
    public LoyaltyBonusTimer getLoyaltyTimer(UUID uuid) {
        return playerManager.getLoyaltyTimer(uuid);
    }

    @Override
    public void add1MinutePlaytime(Player player) throws Exception {
        playerManager.add1MinutePlaytime(player);
    }

    @Override
    public void addMoney(Player player, int amount, String reason) throws Exception {
        playerManager.addMoney(player, amount, reason);
    }

    @Override
    public void removeMoney(Player player, int amount, String reason) throws Exception {
        playerManager.removeMoney(player, amount, reason);
    }

    @Override
    public void addBankMoney(Player player, int amount, String reason) throws Exception {
        playerManager.addBankMoney(player, amount, reason);
    }

    @Override
    public void removeBankMoney(Player player, int amount, String reason) throws Exception {
        playerManager.removeBankMoney(player, amount, reason);
    }

    @Override
    public void setRang(UUID uuid, String rank) {
        playerManager.setRang(uuid, rank);
    }

    @Override
    public int money(Player player) {
        return playerManager.money(player);
    }

    @Override
    public int bank(Player player) {
        return playerManager.bank(player);
    }

    @Override
    public String firstname(Player player) {
        return playerManager.firstname(player);
    }

    @Override
    public String lastname(Player player) {
        return playerManager.lastname(player);
    }

    @Override
    public int visum(Player player) {
        return playerManager.visum(player);
    }

    @Override
    public int paydayDuration(Player player) {
        return playerManager.paydayDuration(player);
    }

    @Override
    public void setPlayerMove(Player player, Boolean state) {
        playerManager.setPlayerMove(player, state);
    }

    @Override
    public boolean canPlayerMove(Player player) {
        return playerManager.canPlayerMove(player);
    }

    @Override
    public boolean isTeam(Player player) {
        return playerManager.isTeam(player);
    }

    @Override
    public Integer perms(Player player) {
        return playerManager.perms(player);
    }

    @Override
    public String rang(Player player) {
        return playerManager.rang(player);
    }

    @Override
    public void startTimeTracker() {
        playerManager.startTimeTracker();
    }

    @Override
    public void kickPlayer(Player player, String reason) {
        playerManager.kickPlayer(player, reason);
    }

    @Override
    public void clearExpBoost(Player player) throws Exception {
        playerManager.clearExpBoost(player);
    }

    @Override
    public void addExp(Player player, Integer exp) {
        playerManager.addExp(player, exp);
    }

    @Override
    public void addExp(Player player, EXPType expType, Integer amount) {
        playerManager.addExp(player, expType, amount);
    }

    @Override
    public void addEXPBoost(Player player, int hours) throws Exception {
        playerManager.addEXPBoost(player, hours);
    }

    @Override
    public void redeemRank(Player player, String type, int duration, String duration_type) throws Exception {
        playerManager.redeemRank(player, type, duration, duration_type);
    }

    @Override
    public boolean isInStaatsFrak(Player player) {
        return playerManager.isInStaatsFrak(player);
    }

    @Override
    public void openInterActionMenu(Player player, Player targetplayer) {
        playerManager.openInterActionMenu(player, targetplayer);
    }

    @Override
    public void openFactionInteractionMenu(Player player, Player targetplayer, String faction) {
        playerManager.openFactionInteractionMenu(player, targetplayer, faction);
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        return playerManager.getPlayerData(uuid);
    }

    @Override
    public PlayerData getPlayerData(Player player) {
        return playerManager.getPlayerData(player);
    }

    @Override
    public Collection<PlayerData> getPlayers() {
        return playerManager.getPlayers();
    }

    @Override
    public void addCoins(Player player, int amount) {
        playerManager.addCoins(player, amount);
    }

    @Override
    public void removeCoins(Player player, int amount) {
        playerManager.removeCoins(player, amount);
    }

    @Override
    public void setPlayerSpawn(PlayerData playerData, String spawn) throws Exception {
        playerManager.setPlayerSpawn(playerData, spawn);
    }

    @Override
    public void carryPlayer(Player player, Player target) {
        playerManager.carryPlayer(player, target);
    }

    @Override
    public void removeTargetFromArmorStand(Player player) {
        playerManager.removeTargetFromArmorStand(player);
    }

    @Override
    public boolean isCarrying(org.bukkit.entity.Entity entity) {
        return playerManager.isCarrying(entity);
    }

    @Override
    public int getGeworbenCount(Player player) throws Exception {
        return playerManager.getGeworbenCount(player);
    }

    @Override
    public void kissPlayer(Player player, Player targetplayer) {
        playerManager.kissPlayer(player, targetplayer);
    }

    @Override
    public void setLongTermJob(Player player, LongTermJob longTermJob) {
        playerManager.setLongTermJob(player, longTermJob);
    }
}
