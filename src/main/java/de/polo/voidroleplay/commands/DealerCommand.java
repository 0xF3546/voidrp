package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.Dealer;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.GamePlay.GamePlay;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class DealerCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final GamePlay gamePlay;
    private final LocationManager locationManager;
    private final FactionManager factionManager;
    public DealerCommand(PlayerManager playerManager, GamePlay gamePlay, LocationManager locationManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.gamePlay = gamePlay;
        this.locationManager = locationManager;
        this.factionManager = factionManager;
        Main.registerCommand("dealer", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        for (Dealer dealer : gamePlay.getDealer()) {
            if (locationManager.getDistanceBetweenCoords(player, "dealer-" + dealer.getId()) < 5) {
                open(player, dealer);
            }
        }
        return false;
    }
    private void open(Player player, Dealer dealer) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cDealer", true, true);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(RoleplayItem.COCAINE.getMaterial(), 1, 0, RoleplayItem.COCAINE.getDisplayName(), "§8 ➥§eBenötigt§8: §71 Joint")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (ItemManager.getCustomItemCount(player, RoleplayItem.JOINT) < 1) {
                    player.sendMessage(Main.error + "Du hast davon nicht genug.");
                    player.closeInventory();
                    return;
                }
                ItemManager.removeCustomItem(player, RoleplayItem.JOINT, 1);
                ItemManager.addCustomItem(player, RoleplayItem.COCAINE, 1);
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(RoleplayItem.NOBLE_JOINT.getMaterial(), 1, 0, RoleplayItem.NOBLE_JOINT.getDisplayName(), "§8 ➥§eBenötigt§8: §71 Joint")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (ItemManager.getCustomItemCount(player, RoleplayItem.JOINT) < 1) {
                    player.sendMessage(Main.error + "Du hast davon nicht genug.");
                    player.closeInventory();
                    return;
                }
                ItemManager.removeCustomItem(player, RoleplayItem.JOINT, 1);
                ItemManager.addCustomItem(player, RoleplayItem.NOBLE_JOINT, 1);
            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(RoleplayItem.BOX_WITH_JOINTS.getMaterial(), 1, 0, "§a+" + ServerManager.getPayout("box_dealer") + "$", "§8 ➥§eBenötigt§8: §71 Kiste mit Joints")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (ItemManager.getCustomItemCount(player, RoleplayItem.BOX_WITH_JOINTS) < 1) {
                    player.sendMessage(Main.error + "Du hast davon nicht genug.");
                    player.closeInventory();
                    return;
                }
                FactionData factionData = factionManager.getFactionData(playerData.getFaction());
                int amount = ServerManager.getPayout("box_dealer");
                long percentage = Math.round(amount * 0.1);
                factionData.addBankMoney((int) percentage, "Verkauf (Dealer - " + player.getName() + ")");
                amount = amount - (int) percentage;
                ItemManager.removeCustomItem(player, RoleplayItem.BOX_WITH_JOINTS, 1);
                player.sendMessage("§8[§cDealer§8]§7 Aus dem Verkauf einer Box erhälst du §a" + amount + "$§7. Es gehen §a" + percentage + "$§7 an deine Fraktion.");
                playerData.addMoney(amount);
                player.closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§6Ankauf")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPurchase(player, dealer);
            }
        });
    }

    private void openPurchase(Player player, Dealer dealer) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cDealer (Ankauf)", true, true);
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                open(player, dealer);
            }
        });
        int pearlPrice = ServerManager.getPayout("dealer_pearl");
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.GHAST_TEAR, 1, 0, "§bPerle", "§8 ➥ §a" + pearlPrice + "$")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                if (ItemManager.getCustomItemCount(player, RoleplayItem.PEARL) < 1) {
                    player.sendMessage(Main.error + "Du hast nicht gengu Perlen dabei.");
                    return;
                }
                player.sendMessage("§8[§cDealer§8]§a +" + pearlPrice + "$");
                ItemManager.removeCustomItem(player, RoleplayItem.PEARL, 1);
                playerManager.addMoney(player, pearlPrice);
            }
        });
        int diamondPrice = ServerManager.getPayout("dealer_diamond");
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.DIAMOND, 1, 0, "§bDiamant", "§8 ➥ §a" + diamondPrice + "$")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                if (ItemManager.getItem(player, Material.DIAMOND) < 1) {
                    player.sendMessage(Main.error + "Du hast nicht gengu Diamanten dabei.");
                    return;
                }
                player.sendMessage("§8[§cDealer§8]§a +" + diamondPrice + "$");
                ItemManager.removeCustomItem(player, RoleplayItem.DIAMOND, 1);
                playerManager.addMoney(player, diamondPrice);
            }
        });
    }
}
