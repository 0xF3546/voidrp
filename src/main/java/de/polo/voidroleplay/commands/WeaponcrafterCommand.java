package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
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
public class WeaponcrafterCommand implements CommandExecutor {
    private final LocationManager locationManager;
    private final PlayerManager playerManager;

    public WeaponcrafterCommand(LocationManager locationManager, PlayerManager playerManager) {
        this.locationManager = locationManager;
        this.playerManager = playerManager;
        Main.registerCommand("waffenhersteller", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (locationManager.getDistanceBetweenCoords(player, "weaponcrafter") > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe des Waffenherstellers.");
            return false;
        }
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getSubGroup() == null) {
            player.sendMessage(Prefix.ERROR + "Nur Untergruppierungen von Fraktionen können hier was herstellen!");
            return false;
        }
        if (playerData.getSubGroup().getFaction() == null) {
            player.sendMessage(Prefix.ERROR + "Nur Untergruppierungen von Fraktionen können hier was herstellen!");
            return false;
        }
        open(player);
        return false;
    }

    private void open(Player player) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Waffenhersteller");
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.LEATHER_HORSE_ARMOR, 1, 0, "§cMarksman", "§8 ➥ §c200 Waffenteile")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                int count = ItemManager.getCustomItemCount(player, RoleplayItem.WAFFENTEIL);
                if (count < 200) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Waffenteile.");
                    return;
                }
                ItemManager.removeCustomItem(player, RoleplayItem.WAFFENTEIL, 200);
                player.getInventory().addItem(ItemManager.createItem(Material.LEATHER_HORSE_ARMOR, 1, 0, "§7Gepackte Waffe", "§8 ➥ §cMarksman"));
                player.sendMessage(Prefix.MAIN + "Du hast eine Marksman hergestellt.");
            }
        });

        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.LEATHER_HORSE_ARMOR, 1, 0, "§cMarksman-Magazin", "§8 ➥ §c30 Waffenteile")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                int count = ItemManager.getCustomItemCount(player, RoleplayItem.WAFFENTEIL);
                if (count < 30) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Waffenteile.");
                    return;
                }
                ItemManager.removeCustomItem(player, RoleplayItem.WAFFENTEIL, 200);
                player.getInventory().addItem(ItemManager.createItem(RoleplayItem.MAGAZIN.getMaterial(), 1, 0, RoleplayItem.MAGAZIN.getDisplayName(), "§8 ➥ §cMarksman"));
                player.sendMessage(Prefix.MAIN + "Du hast ein Marksman-Magazin hergestellt.");
            }
        });
    }
}
