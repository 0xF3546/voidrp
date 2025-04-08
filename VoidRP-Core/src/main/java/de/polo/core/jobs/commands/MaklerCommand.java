package de.polo.core.jobs.commands;

import de.polo.core.handler.CommandBase;
import de.polo.core.manager.ItemManager;
import de.polo.api.player.VoidPlayer;
import de.polo.core.utils.inventory.CustomItem;
import de.polo.core.utils.inventory.InventoryManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import de.polo.api.player.enums.License;
import de.polo.api.jobs.enums.LongTermJob;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import static de.polo.core.Main.locationManager;
import static de.polo.core.Main.playerManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "makler")
public class MaklerCommand extends CommandBase {
    public static String PREFIX = "§8[§6Makler§8]§7 ";

    public MaklerCommand(@NotNull CommandBase.CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (locationManager.getDistanceBetweenCoords(player, "makler_access") > 5) {
            player.sendMessage(Component.text("§7   ===§8[§6Makler§8]§7==="));
            for (PlayerData targetData : playerManager.getPlayers()) {
                if (targetData.getLongTermJob() == LongTermJob.REAL_ESTATE_BROKER) {
                    player.sendMessage(Component.text("§8 ➥ §e" + targetData.getPlayer().getName()));
                }
            }
            return;
        }
        if (playerData.hasLicense(License.REAL_ESTATE_BROKER)) {
            open(player, playerData);
        } else {
            openBuyMenu(player, playerData);
        }
    }

    private void openJobEntryMenu(VoidPlayer player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, "§8 » §7Makler");
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§aJob annehmen", "§8 ➥ §7Du musst diesen Job noch annehmen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerManager.setLongTermJob(player, LongTermJob.REAL_ESTATE_BROKER);
                open(player, playerData);
            }
        });
    }

    private void open(VoidPlayer player, PlayerData playerData) {
        if (playerData.getLongTermJob() != LongTermJob.REAL_ESTATE_BROKER) {
            openJobEntryMenu(player, playerData);
            return;
        }
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, "§8 » §7Makler");
    }

    private void openBuyMenu(VoidPlayer player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, "§8 » §7Makler");
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§6Lizenz kaufen", "§8 ➥ §a10.000$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < 10000) {
                    player.sendMessage(Component.text(Prefix.ERROR + "Du hast nicht genug Geld dabei."));
                    return;
                }
                playerData.addLicenseToDatabase(License.REAL_ESTATE_BROKER);
                player.sendMessage(Component.text(PREFIX + "§aDu hast die Maklerlizenz erworben."));
                playerData.removeMoney(10000, "Maklerlizenz");
                open(player, playerData);
            }
        });
    }
}
