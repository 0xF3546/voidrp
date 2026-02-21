package de.polo.core.elevators;

import de.polo.api.utils.ApiUtils;
import de.polo.api.utils.ItemBuilder;
import de.polo.api.utils.inventorymanager.CustomItem;
import de.polo.api.utils.inventorymanager.InventoryManager;
import de.polo.api.elevators.Floor;
import de.polo.api.player.VoidPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class ElevatorGUI {
    private final VoidPlayer voidPlayer;
    private final Floor floor;

    public ElevatorGUI(VoidPlayer voidPlayer, Floor floor) {
        this.voidPlayer = voidPlayer;
        this.floor = floor;
    }

    public void open() {
        InventoryManager inventoryManager = new InventoryManager(voidPlayer.getPlayer(), ApiUtils.getMatchingInventorySize(floor.elevator().floors().size()), Component.text("§8 » §aAufzug " + floor.elevator().name()));
        int i = 0;
        for (Floor floors : floor.elevator().floors()) {
            if (floors == floor) {
                inventoryManager.setItem(new CustomItem(i, new ItemBuilder(Material.CHEST)
                        .setName("§8 » §aEtage " + floors.floorNumber())
                        .setLore(List.of("§8 » §7Hier bist du gerade"))
                        .build()) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(i, new ItemBuilder(Material.CHEST)
                        .setName("§8 » §aEtage " + floors.floorNumber())
                        .setLore(List.of("§8 » §7Klicke um zu fahren"))
                        .build()) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        voidPlayer.getPlayer().teleport(floors.location());
                        voidPlayer.getPlayer().closeInventory();
                    }
                });
            }
            i++;
        }
    }
}
