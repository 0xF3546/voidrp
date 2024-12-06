package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.InventoryManager.CustomItem;
import de.polo.voidroleplay.manager.InventoryManager.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChangeSpawnCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;
    public ChangeSpawnCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("changespawn", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 20) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27,"§8 » §bSpawn ändern", true, false);
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(Material.RED_DYE, 1, 0, "§cKrankenhaus")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerManager.setPlayerSpawn(playerData, "Krankenhaus");
                player.sendMessage("§aDu hast deinen Spawn auf Krankenhaus geändert.");
            }
        });
        List<House> access = utils.houseManager.getAccessedHousing(player);
        if (playerData.getFaction() != null) {
            inventoryManager.setItem(new CustomItem(1, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§bFraktion")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerManager.setPlayerSpawn(playerData, playerData.getFaction());
                    player.sendMessage("§aDu hast deinen Spawn zu deiner Fraktionsbasis geändert.");
                }
            });
            int i = 2;
            for (House houseData : access) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§aHaus " + houseData.getNumber())) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        playerManager.setPlayerSpawn(playerData, String.valueOf(houseData.getNumber()));
                        player.sendMessage("§aDu hast deinen Spawn auf Haus " + houseData.getNumber() + " gesetzt,");
                    }
                });
                i++;
            }
        } else {
            int i = 1;
            for (House houseData : access) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§aHaus " + houseData.getNumber())) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        playerManager.setPlayerSpawn(playerData, String.valueOf(houseData.getNumber()));
                        player.sendMessage("§aDu hast deinen Spawn auf Haus " + houseData.getNumber() + " gesetzt,");
                    }
                });
                i++;
            }
        }
        return false;
    }
}
