package de.polo.core.laboratory.gui;

import de.polo.api.Utils.GUI;
import de.polo.api.Utils.ItemBuilder;
import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.laboratory.Laboratory;
import de.polo.api.player.VoidPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

public class LaboratoryGUI implements GUI {
    private final VoidPlayer player;
    private final Laboratory laboratory;
    public LaboratoryGUI(VoidPlayer player, Laboratory laboratory) {
        this.player = player;
        this.laboratory = laboratory;
    }
    @Override
    public void open() {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 9, Component.text("§8 » §cLabor §8| §a" + laboratory.getType().getName()));
        inventoryManager.setItem(new CustomItem(0, new ItemBuilder(Material.PAPER)
                .setName("§aBesitzer")
                .setLore("§8▎ §7" + laboratory.getFaction().getFullname())
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
    }
}
