package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.game.base.farming.PlayerWorkstation;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.Workstation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class WorkstationCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public WorkstationCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("workstation", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);

        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Error: /workstation [Workstation]");
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "bulletproof":
                if (player.getLocation().distance(Workstation.BULLETPROOF.getLocation()) > 5) {
                    player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Westenverarbeiters.");
                    break;
                }
                if (PlayerWorkstation.hasWorkstation(player, Workstation.BULLETPROOF)) {
                    playerData.getWorkStation(Workstation.BULLETPROOF).open();
                } else {
                    openRentRequest(player, Workstation.BULLETPROOF);
                }
                break;
            case "kevlar":
                if (player.getLocation().distance(Workstation.KEVLAR.getLocation()) > 5) {
                    player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Kevlarverarbeiters.");
                    break;
                }
                if (PlayerWorkstation.hasWorkstation(player, Workstation.KEVLAR)) {
                    playerData.getWorkStation(Workstation.KEVLAR).open();
                } else {
                    openRentRequest(player, Workstation.KEVLAR);
                }
                break;
            case "eisen":
                if (player.getLocation().distance(Workstation.IRON.getLocation()) > 5) {
                    player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Eisenverarbeiters.");
                    break;
                }
                if (PlayerWorkstation.hasWorkstation(player, Workstation.IRON)) {
                    playerData.getWorkStation(Workstation.IRON).open();
                } else {
                    openRentRequest(player, Workstation.IRON);
                }
                break;
            case "waffenteile":
                if (player.getLocation().distance(Workstation.WAFFENTEILE.getLocation()) > 5) {
                    player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Waffenteilverarbeiters.");
                    break;
                }
                if (PlayerWorkstation.hasWorkstation(player, Workstation.WAFFENTEILE)) {
                    playerData.getWorkStation(Workstation.WAFFENTEILE).open();
                } else {
                    openRentRequest(player, Workstation.WAFFENTEILE);
                }
                break;
            default:
                break;
        }
        return false;
    }

    private void openRentRequest(Player player, Workstation workstation) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Workstation mieten");
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(workstation.getOutputItem().getMaterial(), 1, 0, "§7Workstation mieten", Arrays.asList("§8 ➥ §7Typ§8:§7 " + workstation.getName(), "§8 ➥ §7Benötigtes Material§8: §7" + workstation.getInputItem().getDisplayName(), "§8 ➥ §7Verarbeitetes Material§8: §7" + workstation.getOutputItem().getDisplayName(), "§8 ➥ §7Verarbeitungswert§8: §7" + workstation.getTickInput() + " zu " + workstation.getTickOutput()))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                player.sendMessage("§8[§7Workstation§8]§a Du hast dich in die " + workstation.getName() + " Workstation eingemietet.");
                PlayerWorkstation playerWorkstation = new PlayerWorkstation(player.getUniqueId(), workstation);
                playerWorkstation.create();
                playerData.addWorkstation(playerWorkstation);
            }
        });
    }
}
