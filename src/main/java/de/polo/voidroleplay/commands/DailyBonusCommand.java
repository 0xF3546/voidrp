package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.LocationManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.enums.CaseType;
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
