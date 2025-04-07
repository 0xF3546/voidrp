package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.TeamSpeak;
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
public class SettingsCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public SettingsCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("settings", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Einstellungen");
        int i = 0;
        if (playerData.getTeamSpeakUID() == null) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§7§mTeamSpeak neu synchronisieren", "§8 ➥ §cDu hast dein TeamSpeak nicht verbunden")) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§7TeamSpeak neu synchronisieren")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    TeamSpeak.reloadPlayer(player.getUniqueId());
                }
            });
        }
        i++;
        if (playerData.getPermlevel() >= 60) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§cAdmin-Nachrichten", "§8 ➥ " + (playerData.isSendAdminMessages() ? "§cDeaktivieren" : "§aAktivieren"))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    playerData.setSendAdminMessages(!playerData.isSendAdminMessages());
                }
            });
            i++;
        }
        return false;
    }
}
