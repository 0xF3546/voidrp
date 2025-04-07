package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.player.enums.License;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import static de.polo.voidroleplay.Main.locationManager;

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
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (locationManager.getDistanceBetweenCoords(player, "fahrschule_access") > 5) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du bist nicht in der nähe der Fahrschule."));
            return;
        }
        if (playerData.hasLicense(License.DRIVER)) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du hast bereits einen Führerschein."));
            return;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §3Fahrschule");
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§6Führerschein kaufen", "§8 ➥ §c2.500$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < 2500) {
                    player.sendMessage(Component.text(Prefix.ERROR + "Du hast nicht genug Geld bei dir."));
                    return;
                }
                player.closeInventory();
                playerData.removeMoney(2500, "Führerschein");
                playerData.addLicenseToDatabase(License.DRIVER);
                player.sendMessage(Component.text("§8[§6Führerschein§8]§a Du hast einen Führerschein erworben."));
            }
        });
    }
}
