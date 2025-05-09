package de.polo.core.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.core.Main;
import de.polo.core.game.base.housing.House;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
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
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §bSpawn ändern"), true, false);
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
