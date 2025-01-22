package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.PlayerWeapon;
import de.polo.voidroleplay.game.base.shops.ShopData;
import de.polo.voidroleplay.game.base.shops.ShopItem;
import de.polo.voidroleplay.manager.*;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.Paymethod;
import de.polo.voidroleplay.utils.enums.ShopType;
import de.polo.voidroleplay.utils.enums.Weapon;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.*;

import static de.polo.voidroleplay.Main.vehicles;
import static de.polo.voidroleplay.Main.weaponManager;

public class ShopCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    private final CompanyManager companyManager;
    private final Map<UUID, List<ShopItem>> shoppingCarts = new HashMap<>();

    public ShopCommand(PlayerManager playerManager, LocationManager locationManager, CompanyManager companyManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.companyManager = companyManager;
        Main.registerCommand("shop", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Prefix.ERROR + "Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;
        int shop = locationManager.isNearShop(player);
        if (shop > 0) {
            ShopData shopData = ServerManager.shopDataMap.get(shop);
            openShop(player, shopData);
        } else {
            player.sendMessage(Prefix.ERROR + "Du befindest dich in der Nähe von keinem Shop.");
        }
        return true;
    }

    private void openShop(Player player, ShopData shopData) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventory = new InventoryManager(player, 54, "§8» §c" + shopData.getName(), true, false);

        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i > 44) {
                inventory.setItem(new CustomItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                    }
                });

                if (i == 45 && playerData.getCompany() != null && shopData.getType() != ShopType.BLACKMARKET) {
                    if (playerData.getCompanyRole() != null && (playerData.getCompanyRole().hasPermission("*") || playerData.getCompanyRole().hasPermission("manage_shop_" + shopData.getId()))) {
                        inventory.setItem(new CustomItem(i, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eBusiness-Übersicht")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                                openBusinessOverview(player, shopData);
                            }
                        });
                    }
                }

                if (i == 53) {
                    inventory.setItem(new CustomItem(i, ItemManager.createItem(Material.CHEST, 1, 0, "§6Warenkorb anzeigen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            openShoppingCart(player, shopData);
                        }
                    });
                }
            }
        }

        int j = 10;
        for (ShopItem item : shopData.getItems()) {
            if (item.getShop() == shopData.getId()) {
                while (inventory.getInventory().getItem(j) != null) {
                    j++;
                }
                inventory.setItem(new CustomItem(j, ItemManager.createItem(item.getMaterial(), 1, 0, item.getDisplayName().replace("&", "§"), "§8 ➥ §a" + item.getPrice() + "$")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        addItemToCart(player, item);
                    }
                });
                j++;
            }
        }
    }

    private void openShoppingCart(Player player, ShopData shopData) {
        List<ShopItem> cart = shoppingCarts.getOrDefault(player.getUniqueId(), new ArrayList<>());
        InventoryManager inventory = new InventoryManager(player, 54, "§8» §cWarenkorb", true, true);

        int index = 0;
        for (ShopItem item : cart) {
            inventory.setItem(new CustomItem(index, ItemManager.createItem(item.getMaterial(), 1, 0, item.getDisplayName().replace("&", "§"), "§8 ➥ §a" + item.getPrice() + "$")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    removeItemFromCart(player, item);
                    openShoppingCart(player, shopData);
                    player.sendMessage(Prefix.infoPrefix + "§7Artikel §c" + item.getDisplayName().replace("&", "§") + "§7 wurde aus deinem Warenkorb entfernt.");
                }
            });
            index++;
        }

        inventory.setItem(new CustomItem(53, ItemManager.createItem(Material.EMERALD, 1, 0, "§aKauf abschließen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                askForPaymethod(player, shopData);
            }
        });

        inventory.setItem(new CustomItem(45, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openShop(player, shopData);
            }
        });
    }

    private void askForPaymethod(Player player, ShopData shopData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §aZahlungsmethode");
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.IRON_INGOT, 1, 0, "§aBar")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                finalizePurchase(player, shopData, Paymethod.CASH);
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.IRON_INGOT, 1, 0, "§aKarte")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                finalizePurchase(player, shopData, Paymethod.CARD);
            }
        });
    }

    private void addItemToCart(Player player, ShopItem item) {
        shoppingCarts.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(item);
        player.sendMessage(Prefix.infoPrefix + "§7Artikel §a" + item.getDisplayName().replace("&", "§") + "§7 wurde dem Warenkorb hinzugefügt.");
    }

    private void removeItemFromCart(Player player, ShopItem item) {
        List<ShopItem> cart = shoppingCarts.get(player.getUniqueId());
        if (cart != null) {
            cart.remove(item);
        }
    }

    private void finalizePurchase(Player player, ShopData shopData, Paymethod paymethod) {
        List<ShopItem> cart = shoppingCarts.getOrDefault(player.getUniqueId(), new ArrayList<>());
        PlayerData playerData = playerManager.getPlayerData(player);
        int totalCost = 0;

        for (ShopItem item : cart) {
            totalCost += item.getPrice();
        }
        if (paymethod == Paymethod.CARD) {
            if (playerManager.bank(player) < totalCost) {
                player.sendMessage(Prefix.ERROR + "§7Du hast nicht genügend Bankguthaben für den Kauf. Benötigt: §a" + totalCost + "$.");
                return;
            }
        } else {
            if (playerManager.money(player) < totalCost) {
                player.sendMessage(Prefix.ERROR + "§7Du hast nicht genügend Bargeld für den Kauf. Benötigt: §a" + totalCost + "$.");
                return;
            }
        }

        player.closeInventory();

        for (ShopItem item : cart) {
            try {
                if (Objects.equals(item.getType(), "weapon")) {
                    Weapon weapon = Weapon.valueOf(item.getSecondType().toUpperCase());
                    weaponManager.giveWeaponToCabinet(player, weapon, 0, weapon.getBaseWear());
                } else if (Objects.equals(item.getType(), "ammo")) {
                    Weapon weapon = Weapon.valueOf(item.getSecondType().toUpperCase());
                    PlayerWeapon playerWeapon = playerData.getWeapon(weapon);
                    if (playerWeapon == null) {
                        player.sendMessage(Prefix.ERROR + "§7Du besitzt diese Waffe nicht.");
                        continue;
                    }
                    weaponManager.giveAmmoToCabinet(playerWeapon, weapon.getMaxAmmo());
                } else if (Objects.equals(item.getType(), "car")) {
                    vehicles.giveVehicle(player, item.getSecondType());
                } else if (Objects.equals(item.getType(), "inventory")) {
                    playerData.getInventory().setSizeToDatabase(playerData.getInventory().getSize() + 25);
                } else {
                    player.getInventory().addItem(ItemManager.createItem(item.getMaterial(), 1, 0, item.getDisplayName().replace("&", "§")));
                }
                if (paymethod == Paymethod.CASH) {
                    playerManager.removeMoney(player, item.getPrice(), "Kauf von: " + item.getDisplayName());
                } else {
                    playerManager.removeBankMoney(player, item.getPrice(), "Kauf von: " + item.getDisplayName());
                }
            } catch (Exception e) {
                player.sendMessage(Prefix.ERROR + "§7Fehler beim Kauf von: §c" + item.getDisplayName().replace("&", "§") + ".");
                e.printStackTrace();
            }
        }

        player.sendMessage(Prefix.infoPrefix + "§7Kauf abgeschlossen! Gesamtkosten: §a" + totalCost + "$.");
        shoppingCarts.remove(player.getUniqueId());
    }

    private void openBusinessOverview(Player player, ShopData shopData) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (shopData.getCompany() != playerData.getCompany()) {
            openBusinessBuyOverview(player, shopData);
            return;
        }

        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §c" + shopData.getName() + " (Business)", true, true);
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openShop(player, shopData);
            }
        });

        // Add other business overview functionalities here
    }

    private void openBusinessBuyOverview(Player player, ShopData shopData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §cBusiness kaufen", true, true);
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.EMERALD_BLOCK, 1, 0, "§aBusiness kaufen für " + shopData.getType().getPrice() + "$")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                if (playerManager.money(player) < shopData.getType().getPrice()) {
                    player.sendMessage(Prefix.ERROR + "§7Du hast nicht genügend Geld, um dieses Business zu kaufen.");
                    return;
                }
                playerManager.removeMoney(player, shopData.getType().getPrice(), "Kauf eines Business");
                player.sendMessage(Prefix.infoPrefix + "§7Du hast das Business §a" + shopData.getName() + " §7erworben.");
                shopData.setCompany(playerManager.getPlayerData(player).getCompany().getId());
            }
        });

        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.RED_WOOL, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openShop(player, shopData);
            }
        });
    }
}
