package de.polo.core.commands;

import de.polo.api.Utils.ItemBuilder;
import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.crew.services.CrewService;
import de.polo.core.location.services.LocationService;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.shop.entities.CrewTakeShop;
import de.polo.core.shop.entities.ShopRob;
import de.polo.core.shop.services.ShopService;
import de.polo.core.storage.PlayerWeapon;
import de.polo.core.game.base.shops.ShopData;
import de.polo.core.game.base.shops.ShopItem;
import de.polo.core.manager.*;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.enums.Paymethod;
import de.polo.core.utils.enums.ShopType;
import de.polo.core.utils.enums.Weapon;
import de.polo.core.vehicles.services.VehicleService;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;

import static de.polo.core.Main.weaponManager;

public class ShopCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final CompanyManager companyManager;
    private final Map<UUID, List<ShopItem>> shoppingCarts = new HashMap<>();

    public ShopCommand(PlayerManager playerManager, CompanyManager companyManager) {
        this.playerManager = playerManager;
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
        LocationService locationService = VoidAPI.getService(LocationService.class);
        int shop = locationService.isNearShop(player);
        if (shop > 0) {
            ShopService service = VoidAPI.getService(ShopService.class);
            ShopData shopData = service.getShop(shop);
            openShop(player, shopData);
        } else {
            player.sendMessage(Prefix.ERROR + "Du befindest dich in der Nähe von keinem Shop.");
        }
        return true;
    }

    private void openShop(Player player, ShopData shopData) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventory = new InventoryManager(player, 54, Component.text("§8» §c" + shopData.getName()), true, false);

        // Rahmen mit schwarzen Glasscheiben füllen
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i > 44) {
                inventory.setItem(new CustomItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                    }
                });
            }
        }

        // Positionen unten links für spezielle Items (45, 46, 47, ...)
        int y = 45;

        // Business-Übersicht
        if (playerData.getCompany() != null && shopData.getType().isBuyable()) {
            if (playerData.getCompanyRole() != null && (playerData.getCompanyRole().hasPermission("*") || playerData.getCompanyRole().hasPermission("manage_shop_" + shopData.getId()))) {
                inventory.setItem(new CustomItem(y, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eBusiness-Übersicht")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        openBusinessOverview(player, shopData);
                    }
                });
                y++;
            }
        }
        ShopService shopService = VoidAPI.getService(ShopService.class);

        // Einnehmen (Crew)
        if (playerData.getCrew() != null && shopData.getType().isTakeable()) {
            inventory.setItem(new CustomItem(y, new ItemBuilder(Material.RED_DYE)
                    .setName("§cEinnehmen (Crew)")
                    .setLore(shopData.getCrewHolder() != null ? "§7" + shopData.getCrewHolder().getName() : "§7Keine Crew")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (shopService.getActiveCrewTakes().stream().anyMatch(x -> x.shop() == shopData)) {
                        player.sendMessage("Der Shop wird bereits eingenommen.", Prefix.ERROR);
                        return;
                    }
                    if (shopData.getCrewHolder() != null) {
                        player.sendMessage("Der Shop wird bereits von einer Crew gehalten.", Prefix.ERROR);
                        return;
                    }
                    VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
                    CrewTakeShop crewTakeShop = new CrewTakeShop(voidPlayer.getData().getCrew(), shopData);
                    shopService.addCrewTake(crewTakeShop);
                    CrewService crewService = VoidAPI.getService(CrewService.class);
                    crewService.sendMessageToMembers(voidPlayer.getData().getCrew(), "Eure Crew hat begonnen den Shop " + shopData.getName() + "  einzunehmen.");
                }
            });
            y++;
        }

        // Ausrauben
        if (shopData.getType().isRobable()) {
            inventory.setItem(new CustomItem(y, ItemManager.createItem(Material.RED_DYE, 1, 0, "§cAusrauben")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (shopService.getActiveRobberies().stream().anyMatch(x -> x.getShop() == shopData)) {
                        player.sendMessage("Der Shop wird bereits ausgeraubt.", Prefix.ERROR);
                        return;
                    }
                    VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
                    ShopRob shopRob = new ShopRob(shopData, voidPlayer);
                    shopService.addRobbery(shopRob);
                    player.sendMessage("Du hast den begonnen den Shop §c" + shopData.getName() + " §7 auszurauben.");
                }
            });
            y++;
        }

        // Warenkorb anzeigen (Slot 53)
        inventory.setItem(new CustomItem(53, ItemManager.createItem(Material.CHEST, 1, 0, "§6Warenkorb anzeigen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openShoppingCart(player, shopData);
            }
        });

        // Shop-Items im Hauptbereich platzieren
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
        InventoryManager inventory = new InventoryManager(player, 54, Component.text("§8» §cWarenkorb"), true, true);

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
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §aZahlungsmethode"));
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
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1, 0);

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
                    VehicleService vehicleService = VoidAPI.getService(VehicleService.class);
                    vehicleService.giveVehicle(player, item.getSecondType());
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

        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8» §c" + shopData.getName() + " (Business)"));
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openShop(player, shopData);
            }
        });

        // Add other business overview functionalities here
    }

    private void openBusinessBuyOverview(Player player, ShopData shopData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8» §cBusiness kaufen"), true, true);
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
