package de.polo.core.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.core.handler.CommandBase;
import de.polo.api.player.VoidPlayer;
import de.polo.core.location.services.LocationService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.manager.ItemManager;
import de.polo.core.utils.Prefix;
import de.polo.api.player.enums.License;
import de.polo.api.jobs.enums.LongTermJob;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import static de.polo.core.Main.playerManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "anwalt")
public class AnwaltCommand extends CommandBase {
    public static String PREFIX = "§8[§6Anwalt§8]§7 ";

    public AnwaltCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (locationService.getDistanceBetweenCoords(player, "anwalt") > 5) {
            player.sendMessage(Component.text("§7   ===§8[§6Anwälte§8]§7==="));
            for (PlayerData targetData : playerManager.getPlayers()) {
                if (targetData.getLongTermJob() == LongTermJob.LAWYER) {
                    player.sendMessage(Component.text("§8 ➥ §e" + targetData.getPlayer().getName()));
                }
            }
            return;
        }
        if (playerData.hasLicense(License.LAWYER)) {
            open(player, playerData);
        } else {
            openBuyMenu(player, playerData);
        }
    }

    private void openJobEntryMenu(VoidPlayer player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §7Anwalt"));
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§aJob annehmen", "§8 ➥ §7Du musst diesen Job noch annehmen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerManager.setLongTermJob(player, LongTermJob.LAWYER);
                open(player, playerData);
            }
        });
    }

    private void open(VoidPlayer player, PlayerData playerData) {
        if (playerData.getLongTermJob() != LongTermJob.LAWYER) {
            openJobEntryMenu(player, playerData);
            return;
        }
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §7Anwalt"));
    }

    private void openBuyMenu(VoidPlayer player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §7Anwalt"));
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§6Lizenz kaufen", "§8 ➥ §a25.000$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerData.getBargeld() < 25000) {
                    player.sendMessage(Component.text(Prefix.ERROR + "Du hast nicht genug Geld dabei."));
                    return;
                }
                playerData.addLicenseToDatabase(License.LAWYER);
                player.sendMessage(Component.text(PREFIX + "§aDu hast die Anwaltslizenz erworben."));
                playerData.removeMoney(25000, "Anwaltslizenz");
                open(player, playerData);
            }
        });
    }
}
