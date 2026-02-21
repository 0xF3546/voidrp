package de.polo.core.faction.commands;

import de.polo.api.utils.inventorymanager.CustomItem;
import de.polo.api.utils.inventorymanager.InventoryManager;
import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
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
public class SetFactionChatColorCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public SetFactionChatColorCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;


        Main.registerCommand("setfactionchatcolor", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (!playerData.isLeader()) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        int i = 0;
        Faction factionData = factionManager.getFactionData(playerData.getFaction());
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text(""));
        for (ChatColor color : ChatColor.values()) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, color + color.name())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    factionManager.setFactionChatColor(factionData.getId(), color);
                }
            });
            i++;
        }
        return false;
    }
}
