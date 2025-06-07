package de.polo.core.oil.gui;

import de.polo.api.Utils.GUI;
import de.polo.api.Utils.ItemBuilder;
import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.oil.OilPump;
import de.polo.api.player.VoidPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class OilPumpGUI implements GUI {
    private final VoidPlayer player;
    private final OilPump oilPump;

    public OilPumpGUI(VoidPlayer player, OilPump oilPump) {
        this.player = player;
        this.oilPump = oilPump;
    }
    @Override
    public void open() {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 9, Component.text("§cÖlpumpe"));
        if (oilPump.getCompany() == null) {
            boolean canBuy = player.getData().getCompany() != null;
            String lore = canBuy ? "§aKaufen" : "§cDu bist in keiner Firma.";
            if (!player.getData().getCompanyRole().hasPermission("*") && !player.getData().getCompanyRole().hasPermission("manage_assets")) {
                lore = "§cDu hast keine Berechtigung.";
                canBuy = false;
            }
            inventoryManager.setItem(new CustomItem(3, new ItemBuilder(Material.PAPER)
                    .setName(!canBuy ? "§a§mÖlpumpe kaufen" : "§aÖlpumpe kaufen")
                    .setLore(lore)
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(3, new ItemBuilder(Material.PAPER)
                    .setName("§aÖlpumpe")
                    .setLore("§7Firma: §a" + oilPump.getCompany().getName())
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.sendMessage(Component.text("§cDu kannst die Ölpumpe nicht kaufen, da sie bereits einer Firma gehört."));
                }
            });
        }
        inventoryManager.setItem(new CustomItem(6, new ItemBuilder(Material.COAL)
                .setName("§aÖl abfüllen")
                .setLore("§7§8▎ §aPreis §8» §7120$/Liter")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
    }
}
