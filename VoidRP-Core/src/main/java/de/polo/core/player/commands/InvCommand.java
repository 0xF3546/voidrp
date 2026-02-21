package de.polo.core.player.commands;

import de.polo.api.utils.inventorymanager.CustomItem;
import de.polo.api.utils.inventorymanager.InventoryManager;
import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.enums.Drug;
import de.polo.core.utils.gameplay.GamePlay;
import de.polo.core.utils.player.ChatUtils;
import de.polo.core.utils.player.PlayerInventoryItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@CommandBase.CommandMeta(name = "inv", usage = "/inv")
public class InvCommand extends CommandBase {

    private static final int INVENTORY_SIZE = 27;
    private static final int ITEMS_PER_PAGE = 14; // freie Slots mittig (nicht Rahmen)
    private static final int[] ITEM_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25
    };

    public InvCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        ChatUtils.sendGrayMessageAtPlayer(player.getPlayer(), player.getName() + " öffnet sein Inventar.");
        openInventory(player, playerData, 0);
    }

    public void openInventory(VoidPlayer player, PlayerData playerData, int page) {
        List<Drug> allDrugs = Arrays.asList(Drug.values());
        int totalPages = (int) Math.ceil((double) allDrugs.size() / ITEMS_PER_PAGE);

        InventoryManager inv = new InventoryManager(player.getPlayer(), INVENTORY_SIZE, Component.text("§8 » §bInventar"));

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allDrugs.size());
        int slotIndex = 0;

        for (int i = start; i < end; i++) {
            Drug drug = allDrugs.get(i);
            PlayerInventoryItem item = playerData.getInventory().getByTypeOrEmpty(drug.getItem());

            List<String> lore = Arrays.asList(
                    "§8 ➥ §7" + item.getAmount() + " Stück",
                    "§8 ➥ §7Klicke zum Konsumieren"
            );

            int slot = ITEM_SLOTS[slotIndex++];
            inv.setItem(new CustomItem(slot, ItemManager.createItem(
                    item.getItem().getMaterial(), 1, 0, item.getItem().getDisplayName(), lore)) {
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
        }

        if (page > 0) {
            inv.setItem(new CustomItem(18, ItemManager.createItem(Material.ARROW, 1, 0, "§cZurück", "")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openInventory(player, playerData, page - 1);
                }
            });
        }

        if (page < totalPages - 1) {
            inv.setItem(new CustomItem(26, ItemManager.createItem(Material.ARROW, 1, 0, "§aWeiter", "")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openInventory(player, playerData, page + 1);
                }
            });
        }
    }
}
