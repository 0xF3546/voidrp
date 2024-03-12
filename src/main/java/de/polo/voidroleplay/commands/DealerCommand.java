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
                open(player);
            }
        }
        return false;
    }
    private void open(Player player) {
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
    }
}
