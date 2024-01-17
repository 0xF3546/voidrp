package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.*;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.listeners.InventoryClickListener;
import de.polo.metropiacity.playerUtils.Shop;
import de.polo.metropiacity.utils.*;
import de.polo.metropiacity.utils.InventoryManager.CustomItem;
import de.polo.metropiacity.utils.InventoryManager.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.Objects;

public class ShopCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    public ShopCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.registerCommand("shop", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        int shop = locationManager.isNearShop(player);
        if (shop > 0) {
            ShopData shopData = ServerManager.shopDataMap.get(shop);
            boolean canAccess = false;
            if (shopData.getFaction() == null) {
                canAccess = true;
            } else {
                PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
                if (playerData.getFaction().equalsIgnoreCase(shopData.getFaction())) {
                    canAccess = true;
                }
            }
            if (canAccess) {
                InventoryManager inventory = new InventoryManager(player, 54, "§8» §c" + locationManager.getShopNameById(shop), true, false);
                Inventory inv = Bukkit.createInventory(player, 54, "§8» §c" + locationManager.getShopNameById(shop));
                for (int i = 0; i < 54; i++) {
                    if (i % 9 == 0 || i % 9 == 8 || i < 9 || i > 44) {
                        inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8"));
                        inventory.setItem(new CustomItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                            }
                        });
                    }
                }
                int j = 10;
                for (ShopItem item : Shop.shopItems) {
                    if (item.getShop() == shop) {
                        if (inv.getItem(j) == null) {
                            inv.setItem(j, ItemManager.createItem(item.getMaterial(), 1, 0, item.getDisplayName().replace("&", "§"), "§8 ➥ §a" + item.getPrice() + "$"));
                            inventory.setItem(new CustomItem(j, ItemManager.createItem(item.getMaterial(), 1, 0, item.getDisplayName().replace("&", "§"), "§8 ➥ §a" + item.getPrice() + "$")) {
                                @Override
                                public void onClick(InventoryClickEvent event) {
                                    BuyItem(player, item.getPrice(), item.getType(), item.getDisplayName(), item.getMaterial(), item.getSecondType(), item.getShop() );
                                }
                            });
                            j++;
                        } else {
                            j++;
                            inv.setItem(j, ItemManager.createItem(item.getMaterial(), 1, 0, item.getDisplayName().replace("&", "§"), "§8 ➥ §a" + item.getPrice() + "$"));
                            inventory.setItem(new CustomItem(j, ItemManager.createItem(item.getMaterial(), 1, 0, item.getDisplayName().replace("&", "§"), "§8 ➥ §a" + item.getPrice() + "$")) {
                                @Override
                                public void onClick(InventoryClickEvent event) {
                                    BuyItem(player, item.getPrice(), item.getType(), item.getDisplayName(), item.getMaterial(), item.getSecondType(), item.getShop() );
                                }
                            });
                            j++;
                        }
                    }
                }
            } else {
                player.sendMessage(Main.error + "Du kannst auf diesen Shop nicht zugreifen.");
            }
        }
        return false;
    }

    private void BuyItem(Player player, int price, String type, String displayName, Material item, String info, int shopId) {
        if (playerManager.money(player) >= price) {
            try {
                if (Objects.equals(type, "weapon")) {
                    String weapon = displayName.toString().replace("&", "").replace("6", "");
                    Main.getInstance().weapons.giveWeaponToPlayer(player, item, WeaponType.NORMAL);
                    player.sendMessage("§8[§6" + locationManager.getShopNameById(shopId) + "§8] §7" + "Danke für deinen Einkauf in höhe von §a" + price+ "$.");
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0);
                    playerManager.removeMoney(player, price, "Kauf der Waffe: " + weapon);
                } else if (Objects.equals(type, "ammo")) {
                    String ammo = info;
                    if (player.getEquipment().getItemInMainHand().getType() == Material.AIR) {
                        player.sendMessage(Main.error + "Bitte halte die Waffe in der Hand!");
                        player.closeInventory();
                        return;
                    }
                    for (WeaponData weaponData : Weapons.weaponDataMap.values()) {
                        if (weaponData.getType().equalsIgnoreCase(ammo)) {
                            if (weaponData.getMaterial().equals(player.getEquipment().getItemInMainHand().getType())) {
                                Main.getInstance().weapons.giveWeaponAmmoToPlayer(player, player.getEquipment().getItemInMainHand(), weaponData.getMaxAmmo());
                                player.sendMessage("§8[§6" + locationManager.getShopNameById(shopId) + "§8] §7" + "Danke für deinen Einkauf in höhe von §a" + price + "$.");
                                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0);
                                playerManager.removeMoney(player, price, "Kauf von Munition: " + weaponData.getType());
                            } else {
                                player.sendMessage(Main.error + "Bitte halte die Waffe in der Hand!");
                                player.closeInventory();
                            }
                            return;
                        }
                    }
                    player.sendMessage(Main.error + "Es konnte keine Waffe zur Munition gefunden werden.");
                } else if (Objects.equals(type, "car")) {
                    Main.getInstance().vehicles.giveVehicle(player, info);
                } else {
                    playerManager.removeMoney(player, price, "Kauf von: " + item);
                    player.getInventory().addItem(ItemManager.createItem(item, 1, 0, displayName.replace("&", "§")));
                    player.sendMessage("§8[§6" + locationManager.getShopNameById(shopId) + "§8] §7" + "Danke für deinen Einkauf in höhe von §a" + price + "$.");
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0);
                }
            } catch (SQLException e) {
                player.sendMessage(Main.error + "Fehler. Bitte kontaktiere die Entwicklung.");
                throw new RuntimeException(e);
            }
        } else {
            player.sendMessage("§6" + locationManager.getShopNameById(shopId) + "§8 » §7" + "Du hast leider nicht genug Bargeld.");
        }
    }
}
