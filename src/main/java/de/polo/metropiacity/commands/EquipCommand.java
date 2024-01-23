package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.dataStorage.WeaponData;
import de.polo.metropiacity.dataStorage.WeaponType;
import de.polo.metropiacity.utils.*;
import de.polo.metropiacity.utils.InventoryManager.CustomItem;
import de.polo.metropiacity.utils.InventoryManager.InventoryManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class EquipCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final LocationManager locationManager;
    private final Weapons weapons;
    public EquipCommand(PlayerManager playerManager, FactionManager factionManager, LocationManager locationManager, Weapons weapons) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        this.weapons = weapons;
        Main.registerCommand("equip", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Main.error + "Du bist in keiner Fraktion.");
            return false;
        }
        if (locationManager.getDistanceBetweenCoords(player, "equip_" + playerData.getFaction()) > 5) {
            player.sendMessage(Main.error + "Du bist nicht in der nähe deines Equip-Punktes.");
            return false;
        }
        openMain(player, playerData);
        return false;
    }

    private void openMain(Player player, PlayerData playerData) {
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §" + factionData.getPrimaryColor() + factionData.getName() + " Equip", true, true);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, "§cWaffen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openWeaponShop(player, playerData, factionData);
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.ARROW, 1, 0, "§cMunition")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openAmmoShop(player, playerData, factionData);
            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.LEATHER_CHESTPLATE, 1, 0, "§cExtra")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openExtraShop(player, playerData, factionData);
            }
        });
    }

    private void openWeaponShop(Player player, PlayerData playerData, FactionData factionData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cWaffen", true, true);
        int sturmgewehrPrice = (int) (factionData.equip.getSturmgewehr() * (100 - factionData.upgrades.getWeapon()) / 100);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, "§cSturmgewehr", "§8 ➥ §a" + sturmgewehrPrice + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                int priceForFaction = (int) (ServerManager.getPayout("equip_sturmgewehr") * (100 - factionData.upgrades.getWeapon()) / 100);
                if (factionData.getBank() < priceForFaction) {
                    player.sendMessage(Main.error + "Deine Fraktion hat nicht genug Geld um diese Waffe zu kaufen.");
                    return;
                }
                if (playerData.getBank() < factionData.equip.getSturmgewehr()) {
                    player.sendMessage(Main.error + "Du hast nicht genug Geld.");
                    return;
                }
                factionData.addBankMoney(sturmgewehrPrice, "Waffenkauf " + player.getName());
                playerData.removeBankMoney(sturmgewehrPrice, "Waffenkauf");
                weapons.giveWeaponToPlayer(player, Material.DIAMOND_HORSE_ARMOR, WeaponType.NORMAL);
            }
        });
    }

    private void openAmmoShop(Player player, PlayerData playerData, FactionData factionData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §cMunition", true, true);
        int sturmgewehrPrice = (int) (factionData.equip.getSturmgewehr_ammo() * (100 - factionData.upgrades.getWeapon()) / 100);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, "§cSturmgewehr-Munition", "§8 ➥ §a" + sturmgewehrPrice + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                    int priceForFaction = (int) (ServerManager.getPayout("equip_sturmgewehr_ammo") * (100 - factionData.upgrades.getWeapon()) / 100);
                    if (factionData.getBank() < priceForFaction) {
                    player.sendMessage(Main.error + "Deine Fraktion ht nicht genug Geld um Munition zu kaufen.");
                    return;
                }
                if (playerData.getBank() < factionData.equip.getSturmgewehr_ammo()) {
                    player.sendMessage(Main.error + "Du hast nicht genug Geld.");
                    return;
                }
                WeaponData weaponData = weapons.getWeaponData(player.getEquipment().getItemInMainHand().getType());
                if (weaponData == null) {
                    player.sendMessage(Main.error + "Halte die Waffe in der Hand.");
                    return;
                }
                weapons.giveWeaponAmmoToPlayer(player, player.getEquipment().getItemInMainHand(), weaponData.getMaxAmmo());
                factionData.addBankMoney(sturmgewehrPrice, "Munitionskauf " + player.getName());
                playerData.removeBankMoney(sturmgewehrPrice, "Munitionskauf");
            }
        });
    }
    private void openExtraShop(Player player, PlayerData playerData, FactionData factionData) {

    }
}
