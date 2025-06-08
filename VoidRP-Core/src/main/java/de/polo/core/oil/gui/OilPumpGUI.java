package de.polo.core.oil.gui;

import de.polo.api.Utils.GUI;
import de.polo.api.Utils.ItemBuilder;
import de.polo.api.Utils.enums.Prefix;
import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.oil.OilPump;
import de.polo.api.player.VoidPlayer;
import de.polo.core.manager.ItemManager;
import de.polo.core.oil.services.OilService;
import de.polo.core.utils.enums.RoleplayItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import static de.polo.core.Main.companyManager;
import static de.polo.core.Main.playerManager;

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
        OilService oilService = VoidAPI.getService(OilService.class);
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 9, Component.text("§cÖlpumpe"));
        if (oilPump.getCompany() == null) {
            boolean canBuy = player.getData().getCompany() != null;
            String lore = canBuy ? "§aKaufen" : "§cDu bist in keiner Firma.";
            if (!player.getData().getCompanyRole().hasPermission("*") && !player.getData().getCompanyRole().hasPermission("manage_assets")) {
                lore = "§cDu hast keine Berechtigung.";
                canBuy = false;
            }
            boolean finalCanBuy = canBuy;
            inventoryManager.setItem(new CustomItem(3, new ItemBuilder(Material.PAPER)
                    .setName(!finalCanBuy ? "§a§mÖlpumpe kaufen" : "§aÖlpumpe kaufen")
                    .setLore(lore)
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (!finalCanBuy) return;
                    if (player.getData().getCompany().getBank() < 75000) return;
                    player.getPlayer().closeInventory();
                    player.getData().getCompany().removeBank(75000);
                    companyManager.sendCompanyMessage(player.getData().getCompany(), "§a" + player.getName() + " §7hat eine Ölpumpe gekauft.");
                    oilPump.setCompany(player.getData().getCompany());
                    oilService.updateOilPump(oilPump);
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(3, new ItemBuilder(Material.PAPER)
                    .setName("§aÖlpumpe")
                    .setLore("§7Firma: §a" + oilPump.getCompany().getName())
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        }
        inventoryManager.setItem(new CustomItem(6, new ItemBuilder(Material.COAL)
                .setName("§aÖl abfüllen")
                .setLore("§7§8▎ §aPreis §8» §7120$/5 Liter")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (oilPump.getOil() < 5) {
                    player.sendMessage("§cDie Ölpumpe hat nicht genug Öl.", Prefix.ERROR);
                    return;
                }
                if (!player.getData().removeMoney(120, "Kauf von 5 Litern Öl Pumpe #" + oilPump.getId())) {
                    player.sendMessage("§cDu hast nicht genug Geld.", Prefix.ERROR);
                    return;
                }
                oilPump.setOil(oilPump.getOil() - 5);
                oilService.updateOilPump(oilPump);
                ItemManager.addCustomItem(player.getPlayer(), RoleplayItem.UNPROCESSED_OIL, 5);
            }
        });
    }
}
