package de.polo.core.pinwheels;

import de.polo.api.Utils.ItemBuilder;
import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.pinwheels.Pinwheel;
import de.polo.api.player.VoidPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class PinwheelGUI {
    private final VoidPlayer player;
    private final Pinwheel pinwheel;

    public PinwheelGUI(VoidPlayer player, Pinwheel pinwheel) {
        this.player = player;
        this.pinwheel = pinwheel;
    }

    public void open() {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 9, Component.text("§8 » §aWindrad" + pinwheel.getName()));
        if (pinwheel.isBroken()) {
            for (int i = 0; i < 9; i++) {
                inventoryManager.setItem(new CustomItem(i, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                        .setName(Component.text("§8 » §cWindrad ist defekt"))
                        .build()) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            }
        } else {
            for (int i = 0; i < 9; i++) {
                inventoryManager.setItem(new CustomItem(i, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                        .setName(Component.text("§8 » §aWindrad ist in betrieb"))
                        .build()) {
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            }
        }
    }
}
