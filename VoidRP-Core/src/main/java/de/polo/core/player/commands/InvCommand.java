package de.polo.core.commands;

import de.polo.core.handler.CommandBase;
import de.polo.core.manager.ItemManager;
import de.polo.api.player.VoidPlayer;
import de.polo.core.utils.inventory.CustomItem;
import de.polo.core.utils.inventory.InventoryManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.enums.Drug;
import de.polo.core.utils.gameplay.GamePlay;
import de.polo.core.utils.player.ChatUtils;
import de.polo.core.utils.player.PlayerInventoryItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

@CommandBase.CommandMeta(name = "inv", usage = "/inv")
public class InvCommand extends CommandBase {

    public InvCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, "§8 » §bInventar");
        int i = 0;
        ChatUtils.sendGrayMessageAtPlayer(player.getPlayer(), player.getName() +  " öffnet sein Inventar.");
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
                    GamePlay.useDrug(player.getPlayer(), drug);
                    player.getPlayer().closeInventory();
                }
            });
            i++;
        }
    }
}
