package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.Drug;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.utils.gameplay.GamePlay;
import de.polo.voidroleplay.utils.player.PlayerInventoryItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class InvCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    public InvCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("inv", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §bInventar");
        int i = 0;
        for (Drug drug : Drug.values()) {
            PlayerInventoryItem item = playerData.getInventory().getByTypeOrEmpty(drug.getItem());
            inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(item.getItem().getMaterial(), 1, 0, item.getItem().getDisplayName(), "§8 ➥ §7" + item.getAmount() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (item.getAmount() < 1) {
                        player.sendMessage(Prefix.ERROR + "Du hast nicht genug dabei.");
                        return;
                    }
                    GamePlay.useDrug(player, drug);
                }
            });
            i++;
        }
        return false;
    }
}
