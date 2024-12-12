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
import de.polo.voidroleplay.utils.enums.ShopType;
import de.polo.voidroleplay.utils.enums.Weapon;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

public class ShopCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    private final CompanyManager companyManager;

    public ShopCommand(PlayerManager playerManager, LocationManager locationManager, CompanyManager companyManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.companyManager = companyManager;
        Main.registerCommand("shop", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        int shop = locationManager.isNearShop(player);
        if (shop > 0) {
            ShopData shopData = ServerManager.shopDataMap.get(shop);
            openShop(player, shopData);
        }
        return false;
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
                if (shopData.getType() == null) continue;
                if (i == 45 && playerData.getCompany() != null && shopData.getType() != ShopType.BLACKMARKET) {
                    if (playerData.getCompanyRole().hasPermission("*") || playerData.getCompanyRole().hasPermission("manage_shop_" + shopData.getId())) {
                        inventory.setItem(new CustomItem(i, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eBusiness-Übersicht")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                                openBusinessOverview(player, shopData);
                            }
                        });
                    }
                }
                if (i == 53 && shopData.getType() == ShopType.SUPERMARKET) {
                    inventory.setItem(new CustomItem(i, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§6Zum Ankauf")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            openPurchase(player, shopData);
                        }
                    });
                }
            }
        }
        int j = 10;
        for (ShopItem item : shopData.getItems()) {
            if (item.getShop() == shopData.getId()) {
                if (inventory.getInventory().getItem(j) != null) {
                    j++;
                }
                if (item.getType() != null && item.getType().equalsIgnoreCase("inventory")) {
                    inventory.setItem(new CustomItem(j, ItemManager.createItem(item.getMaterial(), 1, 0, item.getDisplayName().replace("&", "§"), "§8 ➥ §a" + item.getPrice() * (playerData.getInventory().getSize() + 25) + "$")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            BuyItem(player, item.getPrice() * (playerData.getInventory().getSize() + 25), item.getType(), item.getDisplayName(), item.getMaterial(), item.getSecondType(), item.getShop());
                        }
                    });
                } else {
                    inventory.setItem(new CustomItem(j, ItemManager.createItem(item.getMaterial(), 1, 0, item.getDisplayName().replace("&", "§"), "§8 ➥ §a" + item.getPrice() + "$")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            BuyItem(player, item.getPrice(), item.getType(), item.getDisplayName(), item.getMaterial(), item.getSecondType(), item.getShop());
                        }
                    });
                }
                j++;
            }
        }
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
        inventoryManager.setItem(new CustomItem(4, ItemManager.createItem(Material.PAPER, 1, 0, "§3Information", Arrays.asList("§8 ➥ §bKasse§8:§7 " + shopData.getBank() + "$", "§8 ➥ §bProdukte§8:§7 " + shopData.getItems().size()))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        if (playerData.getCompanyRole().hasPermission("*") || playerData.getCompany().getOwner().equals(player.getUniqueId()) || playerData.getCompanyRole().hasPermission("manage_shop_" + shopData.getId())) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.CHEST, 1, 0, "§6Produkte")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openProductManager(player, shopData);
                }
            });
        }
        if (playerData.getCompanyRole().hasPermission("*") || playerData.getCompany().getOwner().equals(player.getUniqueId()) || playerData.getCompanyRole().hasPermission("manage_bank")) {
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§3Kasse leeren")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    companyManager.sendCompanyMessage(playerData.getCompany(), "§8[§6" + playerData.getCompany().getName() + "§8]§7 " + player.getName() + " hat §a" + Utils.toDecimalFormat(shopData.getBank()) + "$ §7aus dem §eShop " + shopData.getName() + "§7 Business entnommen.");
                    playerData.addMoney(shopData.getBank(), "Kasse Shop - " + shopData.getId());
                    playerData.getCompany().setBank(0);
                    playerData.getCompany().save();
                }
            });
        }
    }

    private void openProductManager(Player player, ShopData shopData) {
        PlayerData playerData = playerManager.getPlayerData(player);
    }

    private void openBusinessBuyOverview(Player player, ShopData shopData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §c" + shopData.getName() + " (Business kaufen)", true, true);
        PlayerData playerData = playerManager.getPlayerData(player);
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.PAPER, 1, 0, "§3Statistiken", Arrays.asList("§8 ➥§bTyp§8:§7 " + shopData.getType(), "§8 ➥§bPreis§8:§7 3.250.000$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        if (playerData.getCompanyRole().hasPermission("*")) {
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aKaufen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getCompany().getBank() < 3250000) {
                        player.sendMessage(Prefix.ERROR + "Deine Firma hat nicht genug Kapital um sich dieses Business zu leisten.");
                        return;
                    }
                    player.closeInventory();
                    player.sendMessage("§8[§6" + playerData.getCompany().getName() + "§8]§a Ihr habt das Business \"" + shopData.getType() + " " + shopData.getName() + "\".");
                    shopData.setCompany(playerData.getCompany().getId());
                    shopData.save();
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§a§mKaufen", "§8 ➥ §cDafür bist du nicht berechtigt.")) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        }
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openShop(player, shopData);
            }
        });
    }

    private void openPurchase(Player player, ShopData shopData) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §c" + shopData.getName() + " (Ankauf)", true, true);
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openShop(player, shopData);
            }
        });
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(Material.COD, 1, 0, "§fRoher Kabeljau", "§a+4$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemStack cod = new ItemStack(Material.COD, 1);
                if (player.getInventory().containsAtLeast(cod, 1)) {
                    player.getInventory().removeItem(cod);
                    playerData.addMoney(4, "Verkauf Kabeljau");

                    player.sendMessage("§8[§6" + shopData.getName() + "§8] §7Du hast einen Kabeljau verkauft und 4$ erhalten!");
                } else {
                    player.sendMessage(Prefix.ERROR + "Du hast keinen Kabeljau im Inventar!");
                }
            }
        });
        inventoryManager.setItem(new CustomItem(1, ItemManager.createItem(Material.TROPICAL_FISH, 1, 0, "§fTropenfisch", "§a+25$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemStack cod = new ItemStack(Material.TROPICAL_FISH, 1);
                if (player.getInventory().containsAtLeast(cod, 1)) {
                    player.getInventory().removeItem(cod);
                    playerData.addMoney(25, "Verkauf Tropenfisch");

                    player.sendMessage("§8[§6" + shopData.getName() + "§8] §7Du hast einen Tropenfisch verkauft und 25$ erhalten!");
                } else {
                    // Optional: Benachrichtigung, dass der Spieler keinen Kabeljau hat
                    player.sendMessage(Prefix.ERROR + "Du hast keinen Tropenfisch im Inventar!");
                }
            }
        });
        inventoryManager.setItem(new CustomItem(2, ItemManager.createItem(Material.SALMON, 1, 0, "§fRoher Lachs", "§a+40$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemStack cod = new ItemStack(Material.SALMON, 1);
                if (player.getInventory().containsAtLeast(cod, 1)) {
                    // Kabeljau aus dem Inventar entfernen
                    player.getInventory().removeItem(cod);

                    // Geld dem Spieler hinzufügen
                    playerData.addMoney(40, "Verkauf Roher Lachs");

                    // Optional: Benachrichtigung, dass der Kabeljau verkauft wurde
                    player.sendMessage("§8[§6" + shopData.getName() + "§8] §7Du hast einen Rohen Lachs verkauft und 40$ erhalten!");
                } else {
                    // Optional: Benachrichtigung, dass der Spieler keinen Kabeljau hat
                    player.sendMessage(Prefix.ERROR + "Du hast keinen Rohen Lachs im Inventar!");
                }
            }
        });
        inventoryManager.setItem(new CustomItem(3, ItemManager.createItem(Material.PUFFERFISH, 1, 0, "§fKugelfisch", "§a+85$")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemStack cod = new ItemStack(Material.PUFFERFISH, 1);
                if (player.getInventory().containsAtLeast(cod, 1)) {
                    // Kabeljau aus dem Inventar entfernen
                    player.getInventory().removeItem(cod);

                    // Geld dem Spieler hinzufügen
                    playerData.addMoney(85, "Verkauf Pufferfisch");

                    // Optional: Benachrichtigung, dass der Kabeljau verkauft wurde
                    player.sendMessage("§8[§6" + shopData.getName() + "§8] §7Du hast einen Pufferfisch verkauft und 85$ erhalten!");
                } else {
                    // Optional: Benachrichtigung, dass der Spieler keinen Kabeljau hat
                    player.sendMessage(Prefix.ERROR + "Du hast keinen Pufferfisch im Inventar!");
                }
            }
        });
    }

    private void BuyItem(Player player, int price, String type, String displayName, Material item, String info, int shopId) {
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerManager.money(player) >= price) {
            try {
                if (Objects.equals(type, "weapon")) {
                    try {
                        Weapon w = Weapon.valueOf(info.toUpperCase());
                        String weapon = displayName.replace("&", "").replace("6", "");
                        Main.getInstance().weaponManager.giveWeaponToCabinet(player, w, 0, 250);
                        player.sendMessage("§8[§6" + locationManager.getShopNameById(shopId) + "§8] §7" + "Danke für deinen Einkauf in höhe von §a" + price + "$.");
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0);
                        playerManager.removeMoney(player, price, "Kauf der Waffe: " + weapon);
                    } catch (Exception e) {
                        player.sendMessage(Prefix.ERROR + "Ein Fehler ist aufgetreten.");
                        e.printStackTrace();
                    }
                } else if (Objects.equals(type, "ammo")) {
                    try {
                        Weapon weapon = Weapon.valueOf(info.toUpperCase());
                        PlayerWeapon playerWeapon = playerData.getWeapon(weapon);
                        if (playerWeapon == null) {
                            player.sendMessage(Prefix.ERROR + "Du hast diese Waffe nicht im Waffenschrank.");
                            return;
                        }
                        playerManager.removeMoney(player, price, "Kauf von Munition: " + info);
                        Main.getInstance().weaponManager.giveAmmoToCabinet(playerWeapon, weapon.getMaxAmmo());
                        player.sendMessage("§8[§6" + locationManager.getShopNameById(shopId) + "§8] §7" + "Danke für deinen Einkauf in höhe von §a" + price + "$.");
                    } catch (Exception e) {
                        player.sendMessage(Prefix.ERROR + "Ein Fehler ist aufgetreten.");
                        e.printStackTrace();
                    }
                } else if (Objects.equals(type, "car")) {
                    Main.getInstance().vehicles.giveVehicle(player, info);
                    playerManager.removeMoney(player, price, "Kauf eines Fahrzeuges: " + info);
                } else if (Objects.equals(type, "inventory")){
                    playerData.getInventory().setSizeToDatabase(playerData.getInventory().getSize() + 25);
                    player.sendMessage("§8[§6" + locationManager.getShopNameById(shopId) + "§8] §7" + "Du hast ein Inventar-Upgrade für §a" + price + "$§7 gekauft.");
                    playerManager.removeMoney(player, price, "Kauf eines Inventar-Upgrades: " + info);
                } else {
                    playerManager.removeMoney(player, price, "Kauf von: " + item);
                    player.getInventory().addItem(ItemManager.createItem(item, 1, 0, displayName.replace("&", "§")));
                    player.sendMessage("§8[§6" + locationManager.getShopNameById(shopId) + "§8] §7" + "Danke für deinen Einkauf in höhe von §a" + price + "$.");
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0);
                }
            } catch (SQLException e) {
                player.sendMessage(Prefix.ERROR + "Fehler. Bitte kontaktiere die Entwicklung.");
                throw new RuntimeException(e);
            }
        } else {
            player.sendMessage("§6" + locationManager.getShopNameById(shopId) + "§8 » §7" + "Du hast leider nicht genug Bargeld.");
        }
    }
}
