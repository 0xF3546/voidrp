package de.polo.voidroleplay.game.base.extra.Seasonpass;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.playerUtils.SoundManager;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Seasonpass implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private List<Quest> quests = new ArrayList<>();
    private List<Reward> rewards = new ArrayList<>();
    public Seasonpass(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("seasonpass", this);
        loadAll();
    }

    @SneakyThrows
    public void loadAll() {
        quests.clear();
        rewards.clear();
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM seasonpass_quests");
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            Quest quest = new Quest(result.getInt("id"), result.getInt("points"), result.getString("name"), result.getString("description"), result.getInt("reachedAt"));
            quest.setRewardId(result.getInt("rewardId"));
            quest.setBadFaction(result.getBoolean("isBadFaction"));
            quest.setStaatFaction(result.getBoolean("isStaatFaction"));
            if (result.getString("item") != null) quest.setItem(Material.valueOf(result.getString("item")));
            quests.add(quest);
        }

        statement = connection.prepareStatement("SELECT * FROM seasonpass_rewards");
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
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM seasonpass_player_quests WHERE uuid = ?");
        statement.setString(1, uuid.toString());
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            PlayerQuest playerQuest = new PlayerQuest(result.getInt("id"), result.getInt("questId"), result.getInt("state"));
            playerData.addQuest(playerQuest);
        }
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        while (playerData.getQuests().size() < 14) {
            Quest newQuest = getRandomQuest(playerData);
            if (newQuest == null) {
                player.sendMessage(Prefix.ERROR + "Fehler beim erstellen von neuen Quests.");
                break;
            }

            PlayerQuest playerQuest = new PlayerQuest(newQuest.getId(), 0);
            Connection connection = Main.getInstance().mySQL.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO seasonpass_player_quests (uuid, questId) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.setInt(2, newQuest.getId());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating quest failed, no rows affected.");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    playerQuest.setId(generatedKeys.getInt(1));
                    playerData.addQuest(playerQuest); // Ensure this updates the quest list
                } else {
                    throw new SQLException("Creating PlayerQuest failed, no ID obtained.");
                }
            } finally {
                preparedStatement.close();
                connection.close();
            }
        }

        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Seasonpass");
        int i = -1;
        for (PlayerQuest playerQuest : playerData.getQuests()) {
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

    public Quest getRandomQuest(PlayerData playerData) {
        if (quests == null || quests.isEmpty()) {
            System.out.println("Keine Quests verfügbar.");
            return null;
        }

        List<Quest> availableQuests = new ArrayList<>(quests);
        availableQuests.removeIf(quest -> playerData.getQuests().stream().anyMatch(playerQuest -> playerQuest.getQuestId() == quest.getId()));

        if (availableQuests.isEmpty()) {
            System.out.println("Keine neuen Quests verfügbar.");
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(availableQuests.size());
        Quest quest = availableQuests.get(randomIndex);
        if (quest.isBadFaction()) {
            if (playerData.getFaction() == null) return getRandomQuest(playerData);
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            if (factionData.isBadFrak()) {
                return quest;
            } else {
                return getRandomQuest(playerData);
            }
        }
        if (quest.isStaatFaction()) {
            if (playerData.getFaction() == null) return getRandomQuest(playerData);
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            if (factionData.getName().equalsIgnoreCase("FBI") || factionData.getName().equalsIgnoreCase("Polizei")) {
                return quest;
            } else {
                return getRandomQuest(playerData);
            }
        }
        return quest;
    }

    public void didQuest(Player player, int questId) {
        didQuest(player, questId, 1);
    }

    @SneakyThrows
    public void didQuest(Player player, int questId, int amount) {
        PlayerData playerData = playerManager.getPlayerData(player);
        for (PlayerQuest playerQuest : playerData.getQuests()) {
            if (!(playerQuest.getQuestId() == questId)) continue;
            Quest quest = getQuestById(questId);
            if (playerQuest.getState() >= quest.getReachedAt()) continue;
            playerQuest.setState(playerQuest.getState() + amount);
            if (playerQuest.getState() >= quest.getReachedAt()) {
                Reward reward = getRewardById(quest.getRewardId());
                player.sendMessage("§8[§6Seasonpass§8]§a Du hast die Aufgabe " + quest.getName().replace("&", "§") + " §aabgeschlossen!");
                Main.getInstance().gamePlay.addQuestReward(player, reward.getType(), reward.getAmount(), reward.getInfo());
                SoundManager.successSound(player);
            }
            Connection connection = Main.getInstance().mySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE seasonpass_player_quests SET state = ? WHERE id = ?");
            statement.setInt(1, playerQuest.getState());
            statement.setInt(2, playerQuest.getId());
            statement.executeUpdate();
            statement.close();
            connection.close();
        }
    }

}
