package de.polo.core.player.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.player.VoidPlayer;
import de.polo.api.player.enums.Setting;
import de.polo.core.handler.CommandBase;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.CorePlayerSetting;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.TeamSpeak;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

@CommandBase.CommandMeta(
        name = "settings"
)
public class SettingsCommand extends CommandBase {
    public SettingsCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §7Einstellungen"));
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
                    player.getPlayer().closeInventory();
                    TeamSpeak.reloadPlayer(player.getUuid());
                }
            });
        }
        i++;
        if (playerData.getPermlevel() >= 60) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§cAdmin-Nachrichten", "§8 ➥ " + (player.hasSetting(Setting.TOGGLE_ADMIN_MESSAGES) ? "§cDeaktivieren" : "§aAktivieren"))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.getPlayer().closeInventory();
                    if (player.hasSetting(Setting.TOGGLE_ADMIN_MESSAGES)) {
                        player.removeSetting(Setting.TOGGLE_ADMIN_MESSAGES);
                    } else {
                        player.addSetting(Setting.TOGGLE_ADMIN_MESSAGES);
                    }
                }
            });
            i++;
        }
    }
}
