package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.player.enums.HealthInsurance;
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
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cKrankenkasse");
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
