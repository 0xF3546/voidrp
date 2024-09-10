package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.GamePlay.GamePlay;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.LocationManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class MinerJobCommand implements CommandExecutor {

    private final PlayerManager playerManager;
    private final GamePlay gamePlay;
    private final LocationManager locationManager;
    private final FactionManager factionManager;

    public MinerJobCommand(PlayerManager playerManager, GamePlay gamePlay, LocationManager locationManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.gamePlay = gamePlay;
        this.locationManager = locationManager;
        this.factionManager = factionManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (locationManager.getDistanceBetweenCoords(player, "Miner") < 5) {
            open(player);
        }
        return false;
    }

    private void open(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7Miner", true, true);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.WOODEN_PICKAXE, 1, 0, "§7Holz Spitzhacke")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < 500) {
                    player.sendMessage(Main.error + "Du hast nicht genug geld auf der hand!");
                    return;
                }

                ItemManager.addCustomItem(player, RoleplayItem.MINER_PICKAXE_WOODEN, 1);
                playerData.removeMoney(500, "Holz-Spitzhacke");
            }
        });
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.STONE_PICKAXE, 1, 0, "§7Stein Spitzhacke")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.addonXP.getMinerLevel() < 3) {
                    player.sendMessage(Main.error + "Du musst mindestens Mining Level 3 sein um dir diese Spitzhacke zu kaufen");
                    return;
                }

                if (playerData.getBargeld() < 2500) {
                    player.sendMessage(Main.error + "Du hast nicht genug geld auf der hand!");
                    return;
                }

                ItemManager.addCustomItem(player, RoleplayItem.MINER_PICKAXE_STONE, 1);
                playerData.removeMoney(2500, "Stein-Spitzhacke");
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.IRON_PICKAXE, 1, 0, "§7Eisen Spitzhacke")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.addonXP.getMinerLevel() < 10) {
                    player.sendMessage(Main.error + "Du musst mindestens Mining level 10 sein um dir diese Spitzhacke zu kaufen");
                    return;
                }

                if (playerData.getBargeld() < 15000) {
                    player.sendMessage(Main.error + "Du hast nicht genug geld auf der hand!");
                    return;
                }

                ItemManager.addCustomItem(player, RoleplayItem.MINER_PICKAXE_IRON, 1);
                playerData.removeMoney(15000, "Eisen-Spitzhacke");
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.DIAMOND_PICKAXE, 1, 0, "§7Diamant Spitzhacke")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.addonXP.getMinerLevel() < 15) {
                    player.sendMessage(Main.error + "Du musst mindestens Mining level 15 sein um dir diese Spitzhacke zu kaufen");
                    return;
                }

                if (playerData.getBargeld() < 20000) {
                    player.sendMessage(Main.error + "Du hast nicht genug geld auf der hand!");
                    return;
                }

                ItemManager.addCustomItem(player, RoleplayItem.MINER_PICKAXE_DIA, 1);
                playerData.removeMoney(20000, "Dia-Spitzhacke");
            }
        });
        inventoryManager.setItem(new CustomItem(27, ItemManager.createItem(Material.EMERALD, 1, 0, "§aVerkauf")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openVerkauf(player);
            }
        });
    }

    private void openVerkauf(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7Verkauf", true, true);
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                open(player);
            }
        });
        inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(Material.COAL, 1, 0, "§8Kohle")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
    }

    public void blockBroke(Player player, Block brokenBlock) {

    }
}