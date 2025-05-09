package de.polo.core.base.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.api.player.enums.License;
import de.polo.core.handler.CommandBase;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "fahrschule")
public class FahrschuleCommand extends CommandBase {
    public FahrschuleCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (locationService.getDistanceBetweenCoords(player, "fahrschule_access") > 5) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du bist nicht in der nähe der Fahrschule."));
            return;
        }
        if (playerData.hasLicense(License.DRIVER)) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du hast bereits einen Führerschein."));
            return;
        }
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §3Fahrschule"));
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§6Führerschein kaufen", "§8 ➥ §c2.500$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < 2500) {
                    player.sendMessage(Component.text(Prefix.ERROR + "Du hast nicht genug Geld bei dir."));
                    return;
                }
                player.getPlayer().closeInventory();
                playerData.removeMoney(2500, "Führerschein");
                playerData.addLicenseToDatabase(License.DRIVER);
                player.sendMessage(Component.text("§8[§6Führerschein§8]§a Du hast einen Führerschein erworben."));
            }
        });
    }
}
