package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.Drug;
import de.polo.voidroleplay.utils.gameplay.GamePlay;
import de.polo.voidroleplay.utils.player.ChatUtils;
import de.polo.voidroleplay.utils.player.PlayerInventoryItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

@CommandBase.CommandMeta(name = "inv", usage = "/inv")
public class InvCommand extends CommandBase {

    public InvCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §bInventar");
        int i = 0;
        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() +  " öffnet sein Inventar.");
        for (Drug drug : Drug.values()) {
            PlayerInventoryItem item = playerData.getInventory().getByTypeOrEmpty(drug.getItem());
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(item.getItem().getMaterial(), 1, 0, item.getItem().getDisplayName(), "§8 ➥ §7" + item.getAmount() + " Stück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (item.getAmount() < 1) {
                        player.sendMessage(Prefix.ERROR + "Du hast nicht genug dabei.");
                        return;
                    }
                    playerData.getInventory().removeItem(drug.getItem(), 1);
                    GamePlay.useDrug(player, drug);
                    player.closeInventory();
                }
            });
            i++;
        }
    }
}
