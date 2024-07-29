package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.LocationManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.enums.CaseType;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.Arrays;

public class DailyBonusCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    public DailyBonusCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.registerCommand("dailybonus", this);
    }
    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (locationManager.getDistanceBetweenCoords(player, "dailybonus") > 5) {
            player.sendMessage(Main.error + "Du bist nicht in der nähe des Bonus-Händlers.");
            return false;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §bBonushändler", true, true);

        if (!playerData.hasReceivedBonus()) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aRelease-Bonus erhalten", Arrays.asList("§8 ➥ §b3.000 EXP", "§8 ➥ §a12.500$", "§8 ➥ §e500 Coins", "", "§8 ➥ §aKlicke um mehr auszuwählen"))) {
                @SneakyThrows
                @Override
                public void onClick(InventoryClickEvent event) {
                    openSelectDrug(player);
                }
            });
        }

        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.CHEST, 1, 0, "§bTäglichen Bonus erhalten")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getDailyBonusRedeemed() != null) {
                    if (playerData.getDailyBonusRedeemed().getDayOfMonth() == LocalDateTime.now().getDayOfMonth()) {
                        player.sendMessage(Main.prefix + "Du hast deinen Täglichen Bonus bereits abgeholt.");
                        player.closeInventory();
                        return;
                    }
                }
                if (playerData.getLastPayDay().getDayOfMonth() != LocalDateTime.now().getDayOfMonth()) {
                    player.sendMessage(Main.error + "Du musst mindestens einen PayDay pro Tag erhalten haben um den Täglichen Bonus abzuholen.");
                    player.closeInventory();
                    return;
                }
                getDailyBonus(player);
                player.closeInventory();
            }
        });
        return false;
    }

    @SneakyThrows
    private void openSelectDrug(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);

        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §2Weitere Auswahl", true, true);
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.RED_DYE , 1, 0, "§c+40g Schmerzmittel")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                playerData.setReceivedBonus(true);
                player.sendMessage("§8[§6Release§8]§a Du erhälst 3.000 EXP, 12.500$, 40g Schmerzmittel und 500 Coins.");
                playerManager.addExp(player, 3000);
                playerData.addMoney(12500, "Release-Reward");
                ItemManager.addCustomItem(player, RoleplayItem.SCHMERZMITTEL, 40);
                playerManager.addCoins(player, 500);
                playerManager.addExp(player, 3000);
                Connection connection = Main.getInstance().mySQL.getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE players SET bonusReceived = true WHERE uuid = ?");
                statement.setString(1, player.getUniqueId().toString());
                statement.execute();
                statement.close();
                connection.close();
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.LIME_DYE , 1, 0, "§f+20g Kokain §7&§2 +20 veredelte Joints")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                playerData.setReceivedBonus(true);
                player.sendMessage("§8[§6Release§8]§a Du erhälst 3.000 EXP, 12.500$, 20g Kokain, 20g veredelte Joints und 500 Coins.");
                playerManager.addExp(player, 3000);
                playerData.addMoney(12500, "Release-Reward");
                ItemManager.addCustomItem(player, RoleplayItem.COCAINE, 20);
                ItemManager.addCustomItem(player, RoleplayItem.NOBLE_JOINT, 20);
                playerManager.addCoins(player, 500);
                playerManager.addExp(player, 3000);
                Connection connection = Main.getInstance().mySQL.getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE players SET bonusReceived = true WHERE uuid = ?");
                statement.setString(1, player.getUniqueId().toString());
                statement.execute();
                statement.close();
                connection.close();
            }
        });
    }

    @SneakyThrows
    private void getDailyBonus(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        playerData.setDailyBonusRedeemed(LocalDateTime.now());
        player.getInventory().addItem(ItemManager.createItem(Material.CHEST, 1, 0, CaseType.DAILY.getDisplayName()));
        player.sendMessage(Main.prefix + "Du hast deine Tägliche Case erhalten.");
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE players SET dailyBonusRedeemed = NOW() WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        statement.execute();
        statement.close();
        connection.close();
    }
}
