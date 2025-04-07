package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.CaseType;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

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
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Bonus-Händlers.");
            return false;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §bBonushändler", true, true);

        if (!playerData.hasReceivedBonus()) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aRelease-Bonus erhalten", Arrays.asList("§8 ➥ §b1.000 EXP", "§8 ➥ §a10.000$", "§8 ➥ §e200 Coins", "", "§8 ➥ §aKlicke um mehr auszuwählen"))) {
                @SneakyThrows
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getHours() < 1) {
                        player.sendMessage(Component.text(Prefix.ERROR + "Du musst mindestens 1 Stunde gespielt haben um den Bonus abzuholen."));
                        return;
                    }
                    openSelectDrug(player);
                }
            });
        }

        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.CHEST, 1, 0, "§bTäglichen Bonus erhalten")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getDailyBonusRedeemed() != null) {
                    if (playerData.getDailyBonusRedeemed().getDayOfMonth() == LocalDateTime.now().getDayOfMonth()) {
                        player.sendMessage(Prefix.MAIN + "Du hast deinen Täglichen Bonus bereits abgeholt.");
                        player.closeInventory();
                        return;
                    }
                }
                if (playerData.getLastPayDay().getDayOfMonth() != LocalDateTime.now().getDayOfMonth()) {
                    player.sendMessage(Prefix.ERROR + "Du musst mindestens einen PayDay pro Tag erhalten haben um den Täglichen Bonus abzuholen.");
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
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.RED_DYE, 1, 0, "§c+40g Schmerzmittel")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                playerData.setReceivedBonus(true);
                player.sendMessage("§8[§6Release§8]§a Du erhälst 1.000 EXP, 10.000$, 40g Schmerzmittel und 200 Coins.");
                playerManager.addExp(player, 1000);
                playerData.addMoney(10000, "Release-Reward");
                playerData.getInventory().addItem(RoleplayItem.SCHMERZMITTEL, 40);
                playerManager.addCoins(player, 200);
                playerManager.addExp(player, 1000);
                Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET bonusReceived = true WHERE uuid = ?", player.getUniqueId().toString());
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§7+20 Schnupftabak§7 & §2 +20 Zigarren")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                playerData.setReceivedBonus(true);
                player.sendMessage("§8[§6Release§8]§a Du erhälst 1.000 EXP, 10.000$, 20 Zigarren, 20 Schnupftabak und 200 Coins.");
                playerManager.addExp(player, 1000);
                playerData.addMoney(10000, "Release-Reward");
                playerData.getInventory().addItem(RoleplayItem.SNUFF, 20);
                playerData.getInventory().addItem(RoleplayItem.CIGAR, 20);
                playerManager.addCoins(player, 200);
                playerManager.addExp(player, 1000);
                Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET bonusReceived = true WHERE uuid = ?", player.getUniqueId().toString());
            }
        });
    }

    @SneakyThrows
    private void getDailyBonus(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        playerData.setDailyBonusRedeemed(LocalDateTime.now());
        player.getInventory().addItem(ItemManager.createItem(Material.CHEST, 1, 0, CaseType.DAILY.getDisplayName()));
        player.sendMessage(Prefix.MAIN + "Du hast deine Tägliche Case erhalten.");
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET dailyBonusRedeemed = NOW() WHERE uuid = ?", player.getUniqueId().toString());
    }
}
