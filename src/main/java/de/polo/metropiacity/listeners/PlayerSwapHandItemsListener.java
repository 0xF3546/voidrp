package de.polo.metropiacity.listeners;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.playerUtils.ChatUtils;
import de.polo.metropiacity.utils.InventoryManager.CustomItem;
import de.polo.metropiacity.utils.InventoryManager.InventoryManager;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

public class PlayerSwapHandItemsListener implements Listener {
    private final PlayerManager playerManager;
    private final Utils utils;
    public PlayerSwapHandItemsListener(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);
        if (!player.isSneaking()) {
            return;
        }
        Collection<Entity> entities = player.getWorld().getNearbyEntities(player.getLocation(), 3, 3, 3);
        Item nearestSkull = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : entities) {
            if (entity instanceof Item && entity.getType() == EntityType.DROPPED_ITEM) {
                Item item = (Item) entity;
                if (item.getItemStack().getType() == Material.PLAYER_HEAD) {
                    double distance = item.getLocation().distance(player.getLocation());
                    if (distance < nearestDistance) {
                        if (!item.getCustomName().contains("§8")) {
                            nearestSkull = item;
                            nearestDistance = distance;
                        }
                    }
                }
            }
        }
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("current_inventory", null);
        if (nearestSkull == null) {
            openBag(player);
            return;
        }
        SkullMeta skullMeta = (SkullMeta) nearestSkull.getItemStack().getItemMeta();
        final Item skull = nearestSkull;
        Player targetplayer = Bukkit.getPlayer(skullMeta.getOwningPlayer().getUniqueId());
        System.out.println(targetplayer.getName());
        PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 27, "§8 » §7Bewusstlose Person (" + nearestSkull.getName() + ")");
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Bewusstlose Person (" + nearestSkull.getName() + ")", true ,true);
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.BOOK, 1, 0, "§ePortmonee", Arrays.asList("§8 ➥ §7" + utils.toDecimalFormat(targetplayerData.getBargeld()) + "$", "", "§8[§6Linksklick§8]§7 Geld rauben"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                if (targetplayerData.getBargeld() < 1) {
                    player.sendMessage(Main.error + targetplayer.getName() + " hat kein Bargeld dabei.");
                    return;
                }
                player.sendMessage("§8[§cAusraub§8]§c Du hast " + targetplayer.getName() + " §a" + targetplayerData.getBargeld() + "$§c geklaut.");
                targetplayer.sendMessage("§8[§cAusraub§8]§c " + player.getName() + " hat dir §4" + targetplayerData.getBargeld() + "$§c geklaut.");
                try {
                    playerManager.addMoney(player, targetplayerData.getBargeld());
                    playerManager.removeMoney(targetplayer, targetplayerData.getBargeld(), "Raub (" + player.getName() + ")");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                ChatUtils.sendMeMessageAtPlayer(player, "§o" + player.getName() + " klaut das Bargeld von " + targetplayer.getName() + ".");

            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.RED_DYE, 1, 0, "§cStabilisieren")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.sendMessage("§cDas Feature ist in Arbeit.");
                player.closeInventory();
            }
        });
    }

    private void openBag(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Deine Tasche", true, true);
        inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(Material.BOOK, 1, 0, "§ePortmonee", "§8 ➥ §7" + utils.toDecimalFormat(playerData.getBargeld()) + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.IRON_NUGGET, 1, 0, "§8 » §bHandy öffnen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                utils.phoneUtils.openPhone(player);
            }
        });
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.IRON_INGOT, 1, 0, "§8 » §bTablet öffnen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                utils.tabletUtils.openTablet(player);
            }
        });
        inventoryManager.setItem(new CustomItem(22, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§eCoin-Shop", Arrays.asList("§8 ➥ §7Ränge, Cosmetics und vieles mehr!"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCoinShop(player, playerData);
            }
        });
    }

    private void openCoinShop(Player player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §eCoin-Shop", true, true);
        inventoryManager.setItem(new CustomItem(4, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§6Guthaben", Arrays.asList("§8 ➥ §e" + utils.toDecimalFormat(playerData.getCoins()) + " Coins"))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWUzZDM2YmE4YTI5NjYzZGZkYmVmMTFmOWIyZDExY2FlMzg4Yzc1Nzg0Y2FiYzcwNmRjNjY4OWE4Y2IwYjM1MSJ9fX0=", 1, 0, "§eRänge", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openRankShop(player, playerData);
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTQ4MGQ1N2IwZDFkNDMyZTA3NDg3OGM2YWVjNWY0NWEyY2U5OGQ5YzQ4MWZiOGNjODM4MmNmZjE3MWY4MzY5OSJ9fX0=", 1, 0, "§5Cosmetics", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjE2ZjI3MTQ0ZDhjMmU2NDlhNzZmYjU5NzU3Yzk0ZTQyNTFmMTQ5ZGNhYWFhNzIwZjZmZDZhYTgxY2RlY2MxYSJ9fX0=", 1, 0, "§2Extras", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openExtraShop(player, playerData);
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openBag(player);
            }
        });
    }

    public void openRankShop(Player player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §eRänge", true ,true);
        Inventory inv = Bukkit.createInventory(player, 27, "§8 » §eRänge");
        inventoryManager.setItem(new CustomItem(4, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§6Guthaben", Arrays.asList("§8 ➥ §e" + utils.toDecimalFormat(playerData.getCoins()) + " Coins"))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWUzZDM2YmE4YTI5NjYzZGZkYmVmMTFmOWIyZDExY2FlMzg4Yzc1Nzg0Y2FiYzcwNmRjNjY4OWE4Y2IwYjM1MSJ9fX0=", 1, 0, "§6VIP", Arrays.asList("§8 » §e30 Tage", "§8 » §e20.000 Coins"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                buy(player, "vip_30");
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWUzZDM2YmE4YTI5NjYzZGZkYmVmMTFmOWIyZDExY2FlMzg4Yzc1Nzg0Y2FiYzcwNmRjNjY4OWE4Y2IwYjM1MSJ9fX0=", 1, 0, "§bPremium", Arrays.asList("§8 » §e30 Tage", "§8 » §e10.000 Coins"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                buy(player, "premium_30");
            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWUzZDM2YmE4YTI5NjYzZGZkYmVmMTFmOWIyZDExY2FlMzg4Yzc1Nzg0Y2FiYzcwNmRjNjY4OWE4Y2IwYjM1MSJ9fX0=", 1, 0, "§eGold", Arrays.asList("§8 » §e30 Tage", "§8 » §e5.000 Coins"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                buy(player, "gold_30");
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCoinShop(player, playerData);
            }
        });
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§c"));
            }
        }
        player.openInventory(inv);
    }

    private void openExtraShop(Player player, PlayerData playerData) {
        playerData.setVariable("current_inventory", "coinshop_extras");
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §2Extras", true, true);
        inventoryManager.setItem(new CustomItem(4,                 ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§6Guthaben", Arrays.asList("§8 ➥ §e" + utils.toDecimalFormat(playerData.getCoins()) + " Coins"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
            }
        });
        inventoryManager.setItem(new CustomItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmY4MTIxMTJkZDE4N2U3YzhkZGI1YzNiOGU4NTRlODJmMTkxOTc0MTRhOGNkYjU0MjAyMWYxYTQ5MTg5N2U1MyJ9fX0=", 1, 0, "§bHausslot", Arrays.asList("§8 » §e4.000 Coins"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                buy(player, "hausslot");
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzkyNzRhMmFjNTQxZTQwNGMwYWE4ODg3OWIwYzhiMTBmNTAyYmMyZDdlOWE2MWIzYjRiZjMzNjBiYzE1OTdhMiJ9fX0=", 1, 0, "§3EXP-Boost", Arrays.asList("§8 » §e3 Stunden", "§8 » §e2.000 Coins"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCoinShop(player, playerData);
            }
        });
    }

    private void buy(Player player, String type) {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        switch (type) {
            case "vip_30":
                if (playerData.getCoins() < 20000) {
                    player.sendMessage(Main.error + "Du hast nicht genug Coins (20.000).");
                    player.closeInventory();
                    return;
                }
                try {
                    Main.getInstance().playerManager.removeCoins(player, 20000);
                    Main.getInstance().playerManager.redeemRank(player, "vip", 30, "days");
                    player.closeInventory();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "premium_30":
                if (playerData.getCoins() < 10000) {
                    player.sendMessage(Main.error + "Du hast nicht genug Coins (10.000).");
                    player.closeInventory();
                    return;
                }
                try {
                    Main.getInstance().playerManager.removeCoins(player, 10000);
                    Main.getInstance().playerManager.redeemRank(player, "premium", 30, "days");
                    player.closeInventory();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "gold_30":
                if (playerData.getCoins() < 5000) {
                    player.sendMessage(Main.error + "Du hast nicht genug Coins (5.000).");
                    player.closeInventory();
                    return;
                }
                try {
                    playerManager.removeCoins(player, 5000);
                    playerManager.redeemRank(player, "gold", 30, "days");
                    player.closeInventory();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "hausslot":
                if (playerData.getCoins() < 4000) {
                    player.sendMessage(Main.error + "Du hast nicht genug Coins (4.000).");
                    player.closeInventory();
                    return;
                }
                try {
                    playerManager.removeCoins(player, 4000);
                    utils.housing.addHausSlot(player);
                    player.closeInventory();
                    player.sendMessage("§8[§eCoin-Shop§8]§a Du hast einen Hausslot eingelöst!");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
        }
    }
}
