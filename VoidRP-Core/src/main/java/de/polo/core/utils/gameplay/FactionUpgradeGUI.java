package de.polo.core.utils.gameplay;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.core.faction.entity.Faction;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.RoleplayItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;

public class FactionUpgradeGUI {
    private final FactionManager factionManager;
    private final PlayerManager playerManager;
    private final Utils utils;

    public FactionUpgradeGUI(FactionManager factionManager, PlayerManager playerManager, Utils utils) {
        this.factionManager = factionManager;
        this.playerManager = playerManager;
        this.utils = utils;
    }

    public void open(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        Faction factionData = factionManager.getFactionData(playerData.getFaction());
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §bFraktionsupgrades"), true, true);
        int level = factionData.upgrades.getDrugEarningLevel();
        int upgradeDrugPrice = (int) (Math.pow(2, level - 1) * 400000);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(RoleplayItem.SNUFF.getMaterial(), 1, 0, "§2Drogen-Multiplier", Arrays.asList("§8 ➥§e Aktuell§8:§7 " + factionData.upgrades.getDrugEarning() + "x", "§8 ➥§7Plantagen produzieren mehr Drogen", "", "§8 » §aUpgrade für " + Utils.toDecimalFormat(upgradeDrugPrice) + "$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!playerData.isLeader()) {
                    player.sendMessage(Prefix.ERROR_NOPERMISSION);
                    return;
                }
                if (factionData.getBank() < upgradeDrugPrice) {
                    player.sendMessage(Prefix.ERROR + "Deine Fraktion hat dafür nicht genug Geld.");
                    return;
                }
                factionData.removeFactionMoney(upgradeDrugPrice, "Upgrade-Kauf");
                factionData.upgrades.setDrugEarningLevel(factionData.upgrades.getDrugEarningLevel() + 1);
                factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§3Upgrade§8]§b " + player.getName() + " hat ein Ertrags-Upgrade gekauft!");
                factionData.upgrades.save();
                player.closeInventory();
            }
        });
        int upgradeTaxPrice = (int) (Math.pow(2, level - 1) * 400000);
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§6Steuer-Upgrade", Arrays.asList("§8 ➥§e Aktuell§8:§7 " + Utils.toDecimalFormat(factionData.upgrades.getTax()) + "$", "§8 ➥§7Erhöht den Freibetrag", "", "§8 » §aUpgrade für " + Utils.toDecimalFormat(upgradeTaxPrice) + "$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!playerData.isLeader()) {
                    player.sendMessage(Prefix.ERROR_NOPERMISSION);
                    return;
                }
                if (factionData.getBank() < upgradeTaxPrice) {
                    player.sendMessage(Prefix.ERROR + "Deine Fraktion hat dafür nicht genug Geld.");
                    return;
                }
                factionData.removeFactionMoney(upgradeTaxPrice, "Upgrade-Kauf");
                factionData.upgrades.setTaxLevel(factionData.upgrades.getTaxLevel() + 1);
                factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§3Upgrade§8]§b " + player.getName() + " hat ein Steuer-Upgrade gekauft!");
                factionData.upgrades.save();
                player.closeInventory();
            }
        });
        int upgradeWeaponPrice = (int) (Math.pow(2, level - 1) * 400000);
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, "§cWaffen-Upgrade", Arrays.asList("§8 ➥§e Aktuell§8:§7 " + factionData.upgrades.getWeapon() + "%", "§8 ➥§7Reduziert die Waffenpreise", "", "§8 » §aUpgrade für " + Utils.toDecimalFormat(upgradeWeaponPrice) + "$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!playerData.isLeader()) {
                    player.sendMessage(Prefix.ERROR_NOPERMISSION);
                    return;
                }
                if (factionData.getBank() < upgradeWeaponPrice) {
                    player.sendMessage(Prefix.ERROR + "Deine Fraktion hat dafür nicht genug Geld.");
                    return;
                }
                factionData.removeFactionMoney(upgradeWeaponPrice, "Upgrade-Kauf");
                factionData.upgrades.setWeaponLevel(factionData.upgrades.getWeaponLevel() + 1);
                factionManager.sendCustomMessageToFaction(factionData.getName(), "§8[§3Upgrade§8]§b " + player.getName() + " hat ein Waffen-Upgrade gekauft!");
                factionData.upgrades.save();
                player.closeInventory();
            }
        });
    }
}
