package de.polo.core.player.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.core.Main;
import de.polo.core.handler.CommandBase;
import de.polo.core.manager.ItemManager;
import de.polo.api.player.VoidPlayer;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import de.polo.api.player.enums.HealthInsurance;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import static de.polo.core.Main.locationManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "healthinsurance")
public class HealthInsuranceCommand extends CommandBase {
    public HealthInsuranceCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (locationManager.getDistanceBetweenCoords(player, "healthinsurance") > 5) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du bist nicht in der nähe der Versicherung."));
            return;
        }
        openHealthInsurance(player.getPlayer(), playerData);
    }

    private void openHealthInsurance(Player player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §cKrankenkasse"));
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.IRON_INGOT, 1, 0, "§c" + HealthInsurance.BASIC.getName(), "§8 ➥ §a" + HealthInsurance.BASIC.getPrice() + "$/PayDay")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                setHealthInsurance(player, playerData, HealthInsurance.BASIC);
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§c" + HealthInsurance.PLUS.getName(), "§8 ➥ §a" + HealthInsurance.PLUS.getPrice() + "$/PayDay")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                setHealthInsurance(player, playerData, HealthInsurance.PLUS);
            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.DIAMOND, 1, 0, "§c" + HealthInsurance.FULL.getName(), "§8 ➥ §a" + HealthInsurance.FULL.getPrice() + "$/PayDay")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                setHealthInsurance(player, playerData, HealthInsurance.FULL);
            }
        });
    }

    private void setHealthInsurance(Player player, PlayerData playerData, HealthInsurance insurance) {
        player.sendMessage(Component.text("§8[§cKrankenkasse§8]§a Du hast nun mit \"" + insurance.getName() + "\" versichert."));
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET healthInsurance = ? WHERE uuid = ?", insurance.name(), player.getUniqueId().toString());
    }
}
