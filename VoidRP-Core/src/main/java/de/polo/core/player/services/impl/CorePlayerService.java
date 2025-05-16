package de.polo.core.player.services.impl;

import de.polo.api.Utils.ApiUtils;
import de.polo.api.VoidAPI;
import de.polo.api.jobs.enums.LongTermJob;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.api.player.JobSkill;
import de.polo.api.player.PlayerSetting;
import de.polo.api.player.VoidPlayer;
import de.polo.api.player.enums.Setting;
import de.polo.core.Main;
import de.polo.core.game.base.extra.PlaytimeReward;
import de.polo.core.player.entities.CorePlayerSetting;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.PlayerService;
import de.polo.core.storage.LoyaltyBonusTimer;
import de.polo.core.utils.Service;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.EXPType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static de.polo.core.Main.database;
import static de.polo.core.Main.playerManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
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
        database.deleteAsync("DELETE FROM player_playtime_daily WHERE date < CURDATE() - INTERVAL 7 DAY");
        playerManager.loadPlayer(player);
    }

    @Override
    public void savePlayer(VoidPlayer player) {
        long playtime = Duration.between(player.getRuntimeStatistic().joinTime(), Utils.getTime()).toMinutes();
        database.queryThreaded("INSERT INTO player_playtime_daily (uuid, date, minutes)\n" +
                "SELECT ?, CURDATE(), ?\n" +
                "WHERE NOT EXISTS (\n" +
                "    SELECT 1 FROM player_playtime_daily WHERE uuid = ? AND date = CURDATE()\n" +
                ");", player.getUuid().toString(), playtime, player.getUuid().toString());
        try {
            playerManager.savePlayer(player.getPlayer());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
    public void setLongTermJob(VoidPlayer player, LongTermJob longTermJob) {
        playerManager.setLongTermJob(player, longTermJob);
    }

    @Override
    public void handleJobFinish(VoidPlayer player, MiniJob job, int cooldown, int exp) {
        player.setMiniJob(null);
        player.setActiveJob(null);
        Main.getInstance().getCooldownManager().setCooldown(player.getPlayer(), job.name(), cooldown);
        addExp(player.getPlayer(), exp);
        JobSkill skill = player.getData().getJobSkill(job);
        skill.addExp(exp / Utils.random(2, 3));
    }

    @Override
    public boolean isInJobCooldown(VoidPlayer player, MiniJob job) {
        return Main.getInstance().getCooldownManager().isOnCooldown(player.getPlayer(), job.name());
    }

    @Override
    public int getJobCooldown(VoidPlayer player, MiniJob job) {
        return Main.getInstance().getCooldownManager().getRemainingTime(player.getPlayer(), job.name());
    }

    @Override
    public List<VoidPlayer> getPlayersInRange(Location location, int range) {
        return VoidAPI.getPlayers()
                .stream()
                .filter(voidPlayer -> voidPlayer.getPlayer().getLocation().distance(location) <= range)
                .toList();
    }

    @Override
    public CompletableFuture<List<PlayerSetting>> getPlayerSettings(Player player) {
        return database.executeQueryAsync("SELECT * FROM player_settings WHERE uuid = ?", player.getUniqueId().toString())
                .thenApply(result -> {
                    List<PlayerSetting> settings = new ObjectArrayList<>();
                    for (Map<String, Object> row : result) {
                        PlayerSetting playerSetting = new CorePlayerSetting(
                                Setting.valueOf((String) row.get("setting")),
                                (String) row.get("value")
                        );
                        settings.add(playerSetting);
                    }
                    return settings;
                });
    }


    @Override
    public void addPlayerSetting(VoidPlayer player, PlayerSetting setting) {
        database.insertAsync("INSERT INTO player_settings (uuid, setting, value) VALUES (?, ?, ?)",
                player.getUuid().toString(), setting.getSetting().name(), setting.getValue());
    }

    @Override
    public void removePlayerSetting(VoidPlayer player, PlayerSetting setting) {
        database.deleteAsync("DELETE FROM player_settings WHERE uuid = ? AND setting = ?",
                player.getUuid().toString(), setting.getSetting().name());
    }

    @Override
    public void setLoginStreak(VoidPlayer player, int streak) {
        database.updateAsync("UPDATE players SET loginStreak = ? WHERE uuid = ?",
                streak, player.getUuid().toString());
        player.getData().setLoginStreak(streak);
    }

    @Override
    public void setNaviColor(VoidPlayer player, Color color) {
        database.updateAsync("UPDATE players SET naviColor = ? WHERE uuid = ?",
                ApiUtils.getColorString(color), player.getUuid().toString());
        player.getData().setNaviColor(color);
    }
}
