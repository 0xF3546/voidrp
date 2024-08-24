package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.CaseType;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class VoteShopCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    public VoteShopCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("voteshop", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §eVoteshop");
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(Material.EXPERIENCE_BOTTLE, 1, 0, "§e2.500 EXP", "§8 ➥ §e12 Votes")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getVotes() < 12) {
                    player.sendMessage(Prefix.ERROR + "Du benötigst 12 Votes!");
                    return;
                }
                player.sendMessage("§8[§eVoteshop§8]§7 Du hast 2.500 EXP eingelöst.");
                playerManager.addExp(player, 2500);
                playerData.setVotes(playerData.getVotes() - 12);
                playerData.save();
            }
        });
        inventoryManager.setItem(new CustomItem(1, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§610.000$", "§8 ➥ §e10 Votes")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getVotes() < 10) {
                    player.sendMessage(Prefix.ERROR + "Du benötigst 10 Votes!");
                    return;
                }
                player.sendMessage("§8[§eVoteshop§8]§7 Du hast 10.000$ eingelöst.");
                playerData.addMoney(10000, "Voteshop");
                playerData.setVotes(playerData.getVotes() - 10);
                playerData.save();
            }
        });
        inventoryManager.setItem(new CustomItem(2, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§67 Tage Premium", "§8 ➥ §e50 Votes")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getVotes() < 50) {
                    player.sendMessage(Prefix.ERROR + "Du benötigst 50 Votes!");
                    return;
                }
                player.sendMessage("§8[§eVoteshop§8]§7 Du hast 7 Tage Premium eingelöst.");
                playerManager.redeemRank(player, "Premium", 7, "d");
                playerData.setVotes(playerData.getVotes() - 50);
                playerData.save();
            }
        });
        inventoryManager.setItem(new CustomItem(3, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§bVotecase", "§8 ➥ §e10 Votes")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getVotes() < 10) {
                    player.sendMessage(Prefix.ERROR + "Du benötigst 10 Votes!");
                    return;
                }
                player.sendMessage("§8[§eVoteshop§8]§7 Du hast 1x Votecase eingelöst.");
                player.getInventory().addItem(ItemManager.createItem(Material.CHEST, 1, 0, CaseType.VOTE.getDisplayName()));
                playerData.setVotes(playerData.getVotes() - 10);
                playerData.save();
            }
        });
        return false;
    }
}
