package de.polo.core.game.base.extra.beginnerpass;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.core.Main;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.player.SoundManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
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
 * @version 1.0.1
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
        try (Connection connection = Main.getInstance().coreDatabase.getConnection()) {
            // Laden der Quests
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM beginnerpass_quests")) {
                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    Quest quest = new Quest(result.getInt("id"), result.getString("name"), result.getString("description"), result.getInt("reachedAt"));
                    quest.setRewardId(result.getInt("rewardId"));
                    if (result.getString("item") != null) quest.setItem(Material.valueOf(result.getString("item")));
                    quests.add(quest);
                }
            }

            // Laden der Belohnungen
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM beginnerpass_rewards")) {
                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    Reward reward = new Reward(result.getInt("id"), result.getString("type"), result.getInt("amount"));
                    reward.setInfo(result.getString("info"));
                    reward.setName(result.getString("name"));
                    rewards.add(reward);
                }
            }
        }
    }

    @SneakyThrows
    public void loadPlayerQuests(UUID uuid) {
        PlayerData playerData = playerManager.getPlayerData(uuid);
        try (Connection connection = Main.getInstance().coreDatabase.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM beginnerpass_player_quests WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                PlayerQuest playerQuest = new PlayerQuest(result.getInt("id"), result.getInt("questId"), result.getInt("state"));
                playerData.addBeginnerQuest(playerQuest);
            }
        }
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Prefix.ERROR + "Dieser Befehl kann nur von Spielern ausgeführt werden.");
            return false;
        }

        PlayerData playerData = playerManager.getPlayerData(player);

        // Sicherstellen, dass der Spieler alle Quests hat
        addRemainingQuests(playerData);

        // Inventar erstellen und anzeigen
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §6Beginnerpass"));
        int slot = 0;
        for (PlayerQuest playerQuest : playerData.getBeginnerQuests()) {
            if (slot >= 27) break; // Sicherheitsmaßnahme

            Quest quest = getQuestById(playerQuest.getQuestId());
            String state = "§7" + playerQuest.getState() + "§8/§7" + quest.getReachedAt();
            if (playerQuest.getState() >= quest.getReachedAt()) {
                state = "§2Abgeschlossen!";
            }
            Reward reward = getRewardById(quest.getRewardId());
            Material icon = quest.getItem() != null ? quest.getItem() : Material.PAPER;

            inventoryManager.setItem(new CustomItem(slot, ItemManager.createItem(icon, 1, 0, quest.getName().replace("&", "§"),
                    Arrays.asList("§7" + quest.getDescription(), "§8 » " + state, "§8 » §6Belohnung§8: " + reward.getName().replace("&", "§")))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    // Aktionen bei Klick auf das Item
                }
            });
            slot++;
        }
        return true;
    }

    public Quest getQuestById(int id) {
        return quests.stream().filter(quest -> quest.getId() == id).findFirst().orElse(null);
    }

    public Reward getRewardById(int id) {
        return rewards.stream().filter(reward -> reward.getId() == id).findFirst().orElse(null);
    }

    public void addRemainingQuests(PlayerData playerData) {
        quests.stream().filter(quest -> !hasPlayerQuest(playerData, quest)).forEach(quest -> addQuest(playerData, quest));
    }

    private boolean hasPlayerQuest(PlayerData playerData, Quest quest) {
        return playerData.getBeginnerQuests().stream().anyMatch(playerQuest -> playerQuest.getQuestId() == quest.getId());
    }

    @SneakyThrows
    public void addQuest(PlayerData playerData, Quest quest) {
        Player player = playerData.getPlayer();
        String insertQuery = "INSERT INTO beginnerpass_player_quests (uuid, questId) VALUES (?, ?)";

        Main.getInstance().getCoreDatabase().insertAndGetKeyAsync(insertQuery, player.getUniqueId().toString(), quest.getId()).thenAccept(optionalKey -> {
            if (optionalKey.isPresent()) {
                int questId = optionalKey.get();
                PlayerQuest playerQuest = new PlayerQuest(questId, quest.getId(), 0);
                playerData.addBeginnerQuest(playerQuest);
            } else {
                throw new RuntimeException("Creating PlayerQuest failed, no ID obtained.");
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public void didQuest(Player player, int questId) {
        didQuest(player, questId, 1);
    }

    @SneakyThrows
    public void didQuest(Player player, int questId, int amount) {
        PlayerData playerData = playerManager.getPlayerData(player);
        for (PlayerQuest playerQuest : playerData.getBeginnerQuests()) {
            if (playerQuest.getQuestId() != questId) continue;

            Quest quest = getQuestById(questId);
            if (playerQuest.getState() >= quest.getReachedAt()) return;

            playerQuest.setState(playerQuest.getState() + amount);
            if (playerQuest.getState() >= quest.getReachedAt()) {
                Reward reward = getRewardById(quest.getRewardId());
                player.sendMessage("§8[§6Beginnerpass§8]§a Du hast die Aufgabe " + quest.getName().replace("&", "§") + " §aabgeschlossen!");
                Main.gamePlay.addQuestReward(player, reward.getType(), reward.getAmount(), reward.getInfo());
                SoundManager.successSound(player);
            }

            Main.getInstance().getCoreDatabase().updateAsync("UPDATE beginnerpass_player_quests SET state = ? WHERE id = ?",
                    playerQuest.getState(),
                    playerQuest.getId());
            return;
        }
    }
}
