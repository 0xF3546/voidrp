package de.polo.voidroleplay.game.base.extra.seasonpass;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.storage.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.player.SoundManager;
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
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class Seasonpass implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final List<Quest> quests = new ObjectArrayList<>();
    private final List<Reward> rewards = new ObjectArrayList<>();

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

        try (Connection connection = Main.getInstance().mySQL.getConnection();
             PreparedStatement questStatement = connection.prepareStatement("SELECT * FROM seasonpass_quests");
             ResultSet questResult = questStatement.executeQuery();
             PreparedStatement rewardStatement = connection.prepareStatement("SELECT * FROM seasonpass_rewards");
             ResultSet rewardResult = rewardStatement.executeQuery()) {

            while (questResult.next()) {
                Quest quest = new Quest(
                        questResult.getInt("id"),
                        questResult.getInt("points"),
                        questResult.getString("name"),
                        questResult.getString("description"),
                        questResult.getInt("reachedAt")
                );
                quest.setRewardId(questResult.getInt("rewardId"));
                quest.setBadFaction(questResult.getBoolean("isBadFaction"));
                quest.setStaatFaction(questResult.getBoolean("isStaatFaction"));
                if (questResult.getString("item") != null) {
                    try {
                        quest.setItem(Material.valueOf(questResult.getString("item")));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Ungültiges Material: " + questResult.getString("item"));
                    }
                }
                quests.add(quest);
            }

            while (rewardResult.next()) {
                Reward reward = new Reward(
                        rewardResult.getInt("id"),
                        rewardResult.getString("type"),
                        rewardResult.getInt("amount")
                );
                reward.setInfo(rewardResult.getString("info"));
                reward.setName(rewardResult.getString("name"));
                rewards.add(reward);
            }

        } catch (SQLException e) {
            System.err.println("Fehler beim Laden der Seasonpass-Daten: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void loadPlayerQuests(UUID uuid) {
        PlayerData playerData = playerManager.getPlayerData(uuid);

        if (playerData == null) {
            System.err.println("PlayerData für UUID " + uuid + " konnte nicht gefunden werden.");
            return;
        }

        try (Connection connection = Main.getInstance().mySQL.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM seasonpass_player_quests WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    PlayerQuest playerQuest = new PlayerQuest(
                            result.getInt("id"),
                            result.getInt("questId"),
                            result.getInt("state")
                    );
                    playerData.addQuest(playerQuest);
                }
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Laden der Spielerquests für UUID " + uuid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Prefix.ERROR + "Dieser Befehl kann nur von Spielern ausgeführt werden.");
            return false;
        }

        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());

        if (playerData == null) {
            player.sendMessage(Prefix.ERROR + "Spielerdaten konnten nicht geladen werden.");
            return false;
        }

        final int maxQuests = 14;

        while (playerData.getQuests().size() < maxQuests) {
            Quest newQuest = getRandomQuest(playerData);

            if (newQuest == null) {
                player.sendMessage(Prefix.ERROR + "Es stehen keine neuen Quests zur Verfügung.");
                break;
            }

            try (Connection connection = Main.getInstance().mySQL.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "INSERT INTO seasonpass_player_quests (uuid, questId) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {

                preparedStatement.setString(1, player.getUniqueId().toString());
                preparedStatement.setInt(2, newQuest.getId());

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    player.sendMessage(Prefix.ERROR + "Fehler beim Erstellen einer neuen Quest.");
                    break;
                }

                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        PlayerQuest playerQuest = new PlayerQuest(generatedKeys.getInt(1), newQuest.getId(), 0);
                        playerData.addQuest(playerQuest);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Fehler beim Hinzufügen einer neuen Quest: " + e.getMessage());
                e.printStackTrace();
            }
        }

        showSeasonPassInventory(player, playerData);
        return true;
    }


    private void showSeasonPassInventory(Player player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Seasonpass");
        int slot = 0;

        for (PlayerQuest playerQuest : playerData.getQuests()) {
            if (slot >= 27) break;

            Quest quest = getQuestById(playerQuest.getQuestId());
            if (quest == null) continue;

            String state = "§7" + playerQuest.getState() + "§8/§7" + quest.getReachedAt();
            if (playerQuest.getState() >= quest.getReachedAt()) {
                state = "§2Abgeschlossen!";
            }

            Reward reward = getRewardById(quest.getRewardId());
            Material iconMaterial = (quest.getItem() != null) ? quest.getItem() : Material.PAPER;
            String itemName = quest.getName().replace("&", "§");
            List<String> lore = Arrays.asList(
                    "§7" + quest.getDescription(),
                    "§8 » " + state,
                    "§8 » §6Belohnung§8: " + reward.getName().replace("&", "§")
            );

            CustomItem questItem = new CustomItem(slot, ItemManager.createItem(iconMaterial, 1, 0, itemName, lore)) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    // Aktionen bei Klick auf das Item
                }
            };

            inventoryManager.setItem(questItem);
            slot++;
        }
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

        List<Quest> availableQuests = new ObjectArrayList<>(quests);
        availableQuests.removeIf(quest -> playerData.getQuests().stream().anyMatch(playerQuest -> playerQuest.getQuestId() == quest.getId()));

        if (availableQuests.isEmpty()) {
            System.out.println("Keine neuen Quests verfügbar.");
            return null;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(availableQuests.size() + 1);
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
