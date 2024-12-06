package de.polo.voidroleplay.game.base.extra.Beginnerpass;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.InventoryManager.CustomItem;
import de.polo.voidroleplay.manager.InventoryManager.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.playerUtils.SoundManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Beginnerpass implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final List<Quest> quests = new ObjectArrayList<>();
    private final List<Reward> rewards = new ObjectArrayList<>();

    public Beginnerpass(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("beginnerpass", this);
        loadAll();
    }

    @SneakyThrows
    public void loadAll() {
        quests.clear();
        rewards.clear();
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM beginnerpass_quests");
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            Quest quest = new Quest(result.getInt("id"), result.getString("name"), result.getString("description"), result.getInt("reachedAt"));
            quest.setRewardId(result.getInt("rewardId"));
            if (result.getString("item") != null) quest.setItem(Material.valueOf(result.getString("item")));
            quests.add(quest);
        }

        statement = connection.prepareStatement("SELECT * FROM beginnerpass_rewards");
        result = statement.executeQuery();
        while (result.next()) {
            Reward reward = new Reward(result.getInt("id"), result.getString("type"), result.getInt("amount"));
            reward.setInfo(result.getString("info"));
            reward.setName(result.getString("name"));
            rewards.add(reward);
        }
        statement.close();
        connection.close();
    }

    @SneakyThrows
    public void loadPlayerQuests(UUID uuid) {
        PlayerData playerData = playerManager.getPlayerData(uuid);
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM beginnerpass_player_quests WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            PlayerQuest playerQuest = new PlayerQuest(result.getInt("id"), result.getInt("questId"), result.getInt("state"));
            playerData.addBeginnerQuest(playerQuest);
        }
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        while (playerData.getBeginnerQuests().size() < quests.size()) {
            addRemainingQuests(playerData);
        }

        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Beginnerpass");
        int i = -1;
        for (PlayerQuest playerQuest : playerData.getBeginnerQuests()) {
            i++;
            Quest quest = getQuestById(playerQuest.getQuestId());
            String state = "§7" + playerQuest.getState() + "§8/§7" + quest.getReachedAt();
            if (playerQuest.getState() >= quest.getReachedAt()) {
                state = "§2Abgeschlossen!";
            }
            Reward reward = getRewardById(quest.getRewardId());
            if (playerQuest.getState() >= quest.getReachedAt()) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.BARRIER, 1, 0, quest.getName().replace("&", "§"), Arrays.asList("§7" + quest.getDescription(), "§8 » " + state, "§8 » §6Belohnung§8: " + reward.getName().replace("&", "§")))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
                continue;
            }
            if (quest.getItem() == null) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, quest.getName().replace("&", "§"), Arrays.asList("§7" + quest.getDescription(), "§8 » " + state, "§8 » §6Belohnung§8: " + reward.getName().replace("&", "§")))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(quest.getItem(), 1, 0, quest.getName().replace("&", "§"), Arrays.asList("§7" + quest.getDescription(), "§8 » " + state, "§8 » §6Belohnung§8: " + reward.getName().replace("&", "§")))) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            }
        }
        return false;
    }

    public Quest getQuestById(int id) {
        for (Quest quest : quests) {
            if (quest.getId() == id) return quest;
        }
        return null;
    }

    public Reward getRewardById(int id) {
        for (Reward reward : rewards) {
            if (reward.getId() == id) return reward;
        }
        return null;
    }

    public void addRemainingQuests(PlayerData playerData) {
        for (Quest quest : quests) {
            if (!hasPlayerQuest(playerData, quest)) {
                addQuest(playerData, quest);
            }
        }
    }

    private boolean hasPlayerQuest(PlayerData playerData, Quest quest) {
        for (PlayerQuest playerQuest : playerData.getBeginnerQuests()) {
            if (playerQuest.getId() == quest.getId()) return true;
        }
        return false;
    }

    @SneakyThrows
    private void addQuest(PlayerData playerData, Quest quest) {
        PlayerQuest playerQuest = new PlayerQuest(quest.getId(), 0);
        Player player = playerData.getPlayer();
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO beginnerpass_player_quests (uuid, questId) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, player.getUniqueId().toString());
        preparedStatement.setInt(2, quest.getId());

        int affectedRows = preparedStatement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating quest failed, no rows affected.");
        }

        try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                playerQuest.setId(generatedKeys.getInt(1));
                playerData.addBeginnerQuest(playerQuest); // Ensure this updates the quest list
            } else {
                throw new SQLException("Creating PlayerQuest failed, no ID obtained.");
            }
        } finally {
            preparedStatement.close();
            connection.close();
        }
    }

    public void didQuest(Player player, int questId) {
        didQuest(player, questId, 1);
    }

    @SneakyThrows
    public void didQuest(Player player, int questId, int amount) {
        PlayerData playerData = playerManager.getPlayerData(player);
        for (PlayerQuest playerQuest : playerData.getBeginnerQuests()) {
            if (!(playerQuest.getQuestId() == questId)) continue;
            Quest quest = getQuestById(questId);
            if (playerQuest.getState() >= quest.getReachedAt()) continue;
            playerQuest.setState(playerQuest.getState() + amount);
            if (playerQuest.getState() >= quest.getReachedAt()) {
                Reward reward = getRewardById(quest.getRewardId());
                player.sendMessage("§8[§6Beginnerpass§8]§a Du hast die Aufgabe " + quest.getName().replace("&", "§") + " §aabgeschlossen!");
                Main.getInstance().gamePlay.addQuestReward(player, reward.getType(), reward.getAmount(), reward.getInfo());
                SoundManager.successSound(player);
            }
            Connection connection = Main.getInstance().mySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE beginnerpass_player_quests SET state = ? WHERE id = ?");
            statement.setInt(1, playerQuest.getState());
            statement.setInt(2, playerQuest.getId());
            statement.executeUpdate();
            statement.close();
            connection.close();
        }
    }
}
