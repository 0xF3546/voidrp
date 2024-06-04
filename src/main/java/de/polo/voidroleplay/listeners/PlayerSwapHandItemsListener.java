package de.polo.voidroleplay.listeners;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.game.base.extra.Storage;
import de.polo.voidroleplay.game.base.vehicle.PlayerVehicleData;
import de.polo.voidroleplay.game.base.vehicle.Vehicles;
import de.polo.voidroleplay.game.faction.gangwar.Gangwar;
import de.polo.voidroleplay.game.faction.gangwar.GangwarUtils;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import de.polo.voidroleplay.utils.enums.StorageType;
import de.polo.voidroleplay.utils.enums.Storages;
import de.polo.voidroleplay.utils.playerUtils.ChatUtils;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.BlockIterator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;

public class PlayerSwapHandItemsListener implements Listener {
    private final PlayerManager playerManager;
    private final Utils utils;

    public PlayerSwapHandItemsListener(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    private Block getTargetBlock(Player player) {
        BlockIterator iterator = new BlockIterator(player, 5);
        Block block = null;
        while (iterator.hasNext()) {
            block = iterator.next();
            if (block.getType().isSolid()) {
                break;
            }
        }
        return block;
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);
        Block block = getTargetBlock(player);
        if (!player.isSneaking()) {
            return;
        }
        for (WeaponData weaponData : Weapons.weaponDataMap.values()) {
            if (player.getInventory().getItemInMainHand().getType().equals(weaponData.getMaterial())) {
                InventoryManager inventoryManager = new InventoryManager(player, 9, "§8 » " + weaponData.getName());
                inventoryManager.setItem(new CustomItem(3, ItemManager.createItem(weaponData.getMaterial(), 1, 0, weaponData.getName(), "§8 ➥ §7Klicke um Waffe zu packen")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        player.closeInventory();
                        Weapon weapon = Main.getInstance().weapons.getWeaponFromItemStack(player.getInventory().getItemInMainHand());
                        int ammoCount = weapon.getAmmo();
                        if (weapon.getCurrentAmmo() >= weaponData.getMaxAmmo()) {
                            ammoCount += weaponData.getMaxAmmo();
                        }
                        ammoCount = ammoCount / weaponData.getMaxAmmo();
                        Main.getInstance().weapons.removeWeapon(player, player.getInventory().getItemInMainHand());
                        if (ammoCount >= 1) {
                            player.getInventory().addItem(ItemManager.createItem(RoleplayItem.MAGAZIN.getMaterial(), ammoCount, 0, "§7Magazin", "§8 ➥ " + weaponData.getName()));
                        }
                        player.getInventory().addItem(ItemManager.createItem(weaponData.getMaterial(), 1, 0, "§7Gepackte Waffe", "§8 ➥ " + weaponData.getName()));
                        player.sendMessage(Prefix.MAIN + "Du hast deine Waffe gepackt.");
                    }
                });
                int count = ItemManager.getCustomItemCount(player, RoleplayItem.MAGAZIN);
                inventoryManager.setItem(new CustomItem(5, ItemManager.createItem(Material.CLAY_BALL, 1, 0, "§7Magazin benutzen", "§8 ➥ §7Du hast " + count + " Magazine")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        player.closeInventory();
                        player.sendMessage(Prefix.MAIN + "Du hast " + count + " Magazine benutzt.");
                        int remainingCount = count;

                        ItemStack[] contents = player.getInventory().getContents();
                        for (ItemStack itemStack : contents) {
                            if (itemStack != null && itemStack.getType() == RoleplayItem.MAGAZIN.getMaterial() && itemStack.getItemMeta().getLore().get(0).replace("§8 ➥ ", "").equalsIgnoreCase(weaponData.getName())) {
                                int stackAmount = itemStack.getAmount();
                                if (stackAmount <= remainingCount) {
                                    remainingCount -= stackAmount;
                                    player.getInventory().removeItem(itemStack);
                                } else {
                                    itemStack.setAmount(stackAmount - remainingCount);
                                    break;
                                }
                                if (remainingCount <= 0) {
                                    break;
                                }
                            }
                        }
                        Main.getInstance().weapons.giveWeaponAmmoToPlayer(player, player.getInventory().getItemInMainHand(), count * weaponData.getMaxAmmo());
                    }
                });
                return;
            }
        }
        for (Storages storage : Storages.values()) {
            if (player.getLocation().distance(storage.getLocation()) < 5) {
                Storage s = Storage.getStorageByTypeAndPlayer(StorageType.EXTRA, player, storage);
                if (s == null) {
                    s = new Storage(StorageType.EXTRA);
                    s.setExtra(storage);
                    s.setPlayer(player.getUniqueId().toString());
                    s.create();
                };
                s.open(player);
                return;
            }
        }
        PlayerVehicleData playerVehicleData = Vehicles.getNearestVehicle(player.getLocation());
        if (playerVehicleData != null) {
            if (!playerVehicleData.isLocked()) {
                if (player.getLocation().distance(playerVehicleData.getLocation()) < 2) {
                    Storage s = Storage.getStorageByTypeAndPlayer(StorageType.VEHICLE, player, playerVehicleData.getId());
                    if (s == null) {
                        s = new Storage(StorageType.VEHICLE);
                        s.setVehicleId(playerVehicleData.getId());
                        s.setPlayer(player.getUniqueId().toString());
                        s.create();
                    }
                    ;
                    s.open(player);
                    return;
                }
            }
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
            for (LocationData locationData : Main.getInstance().locationManager.getLocations()) {
                if (locationData.getType() == null) continue;
                if (locationData.getInfo() == null) continue;
                if (!locationData.getType().equalsIgnoreCase("storage")) {
                    continue;
                }
                if (player.getLocation().distance(new Location(player.getWorld(), locationData.getX(), locationData.getY(), locationData.getZ())) < 2) {
                    openFactionStorage(player);
                    return;
                }
            }
            openBag(player);
            return;
        }
        SkullMeta skullMeta = (SkullMeta) nearestSkull.getItemStack().getItemMeta();
        final Item skull = nearestSkull;
        Player targetplayer = Bukkit.getPlayer(skullMeta.getOwningPlayer().getUniqueId());
        System.out.println(targetplayer.getName());
        PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        if (targetplayerData.getVariable("gangwar") != null) return;
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Bewusstlose Person (" + nearestSkull.getName() + ")", true, true);
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.PAPER, 1, 0, "§7Personalausweis nehmen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                Locale locale = new Locale("de", "DE");
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
                String formattedDate = dateFormat.format(targetplayerData.getBirthday());
                targetplayer.sendMessage("§8 » §7Jemand hat deinen Ausweis genommen.");
                player.sendMessage("§7     ===§8[§6PERSONALAUSWEIS§8]§7===");
                player.sendMessage(" ");
                player.sendMessage("§8 ➥ §eVorname§8:§7 " + targetplayerData.getFirstname());
                player.sendMessage("§8 ➥ §eNachname§8:§7 " + targetplayerData.getFirstname());
                player.sendMessage("§8 ➥ §eGeschlecht§8:§7 " + targetplayerData.getGender().getTranslation());
                player.sendMessage("§8 ➥ §eGeburtsdatum§8:§7 " + formattedDate);
                player.sendMessage(" ");
                player.sendMessage("§8 ➥ §eWohnort§8:§7 " + utils.housing.getHouseAccessAsString(targetplayerData));
            }
        });
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
        if (!targetplayerData.isStabilized()) {
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.RED_DYE, 1, 0, "§cStabilisieren")) {
                @SneakyThrows
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    targetplayerData.setStabilized(true);
                    targetplayerData.setDeathTime(targetplayerData.getDeathTime() + 120);
                    ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " stabilisiert " + targetplayer.getName());
                    Connection connection = Main.getInstance().mySQL.getConnection();
                    PreparedStatement statement = connection.prepareStatement("UPDATE players SET isStabilized = true WHERE uuid = ?");
                    statement.setString(1, targetplayer.getUniqueId().toString());
                    statement.execute();
                    statement.close();
                    connection.close();
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.RED_DYE, 1, 0, "§c§mStabilisieren", "§8 ➥ §7Die Person ist bereits stabilisiert.")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                }
            });
        }
    }

    private void openFactionStorage(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Fraktionslager");
        boolean canTakeout = true;
        for (UUID uuid : ServerManager.factionStorageWeaponsTookout) {
            if (player.getUniqueId().equals(uuid)) canTakeout = false;
        }
        if (canTakeout) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, "§cTägliche Waffe nehmen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    player.sendMessage(Prefix.MAIN + "Du hast deine Tägliche Waffe entnommen");
                    Main.getInstance().weapons.giveWeaponToPlayer(player, Material.DIAMOND_HORSE_ARMOR, WeaponType.NORMAL);
                    ServerManager.factionStorageWeaponsTookout.add(player.getUniqueId());
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.DIAMOND_HORSE_ARMOR, 1, 0, "§c§mTägliche Waffe nehmen", "§8 ➥ §cDu hast deine Waffe bereits abgeholt")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                }
            });
        }
        Gangwar gangwar = null;
        for (Gangwar gw : Main.getInstance().utils.gangwarUtils.getGangwars()) {
            if (gw.getAttacker().equals(playerData.getFaction()) || gw.getDefender().equals(playerData.getFaction()))
                gangwar = gw;
        }
        if (gangwar != null) {
            inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.OAK_SIGN, 1, 0, "§6Gangwar betreten", Arrays.asList("§8 ➥ §eGebiet§8:§7 " + gangwar.getGangZone().getName(), "§8 ➥ §eZeit§8:§7 " + gangwar.getMinutes() + "m & " + gangwar.getSeconds() + "s verbleibend", "§8 ➥ §eAngreifer§8:§7 " + gangwar.getAttacker() + " (" + gangwar.getAttackerPoints() + ")", "§8 ➥ §eVerteitiger§8:§7 " + gangwar.getDefender() + " (" + gangwar.getDefenderPoints() + ")"))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    GangwarUtils gangwarUtils = Main.getInstance().utils.gangwarUtils;
                    FactionManager factionManager = Main.getInstance().factionManager;
                    ;
                    if (playerData.getFaction() == null || Objects.equals(playerData.getFaction(), "Zivilist")) {
                        player.sendMessage(Main.error + "Du bist in keiner Fraktion.");
                        return;
                    }
                    if (playerData.getVariable("gangwar") != null) {
                        player.sendMessage("§8[§cGangwar§8]§c Du bist bereits im Gangwar.");
                        player.closeInventory();
                        return;
                    }
                    FactionData factionData = factionManager.getFactionData(playerData.getFaction());
                    if (factionData.getCurrent_gangwar() != null) {
                        gangwarUtils.joinGangwar(player, factionData.getCurrent_gangwar());
                        Gangwar gangwarData = gangwarUtils.getGangwarByZone(factionData.getCurrent_gangwar());
                        player.sendMessage("§8[§cGangwar§8]§7 Du hast den Gangwar §c" + gangwarData.getGangZone().getName() + "§7 betreten.");
                        factionManager.sendMessageToFaction(playerData.getFaction(), player.getName() + " ist dem Gangwar beigetreten.");
                    } else {
                        player.sendMessage("§8[§cGangwar§8]§c Deine Fraktion befindet sich aktuell in keinem Gangwar.");
                    }
                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.OAK_SIGN, 1, 0, "§6§mGangwar betreten", "§8 ➥ §cDeine Fraktion befindet sich in keinem Gangwar!")) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        }
    }

    private void openBag(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Deine Tasche", true, true);
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.BOOK, 1, 0, "§ePortmonee", "§8 ➥ §7" + utils.toDecimalFormat(playerData.getBargeld()) + "$")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.IRON_NUGGET, 1, 0, "§8 » §bHandy öffnen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                utils.phoneUtils.openPhone(player);
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.IRON_INGOT, 1, 0, "§8 » §bTablet öffnen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                utils.tabletUtils.openTablet(player);
            }
        });
        inventoryManager.setItem(new CustomItem(21, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§eCoin-Shop", Arrays.asList("§8 ➥ §7Ränge, Cosmetics und vieles mehr!"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCoinShop(player, playerData);
            }
        });
        inventoryManager.setItem(new CustomItem(23, ItemManager.createItemHead(player.getUniqueId().toString(), 1, 0, "§bStatistiken", "§8 ➥ §7Übersicht zu Spieler & Character Statistiken")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openStatistics(player, playerData);
            }
        });
    }

    private void openStatistics(Player player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §eStatistiken", true, true);
        inventoryManager.setItem(new CustomItem(4, ItemManager.createItemHead(player.getUniqueId().toString(), 1, 0, "§6" + player.getName(), Arrays.asList("§8 ➥ §eLevel§8:§7 " + playerData.getLevel() + " (" + playerData.getExp() + "/" + playerData.getNeeded_exp() + ")", "§8 ➥ §eVisum§8:§7 " + playerData.getVisum() + " (" + playerData.getCurrentHours() + "/" + (playerData.getVisum() * 4) + ")"))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        String faction = playerData.getFaction() + "(" + playerData.getFactionGrade() + "/8)";
        if (playerData.getFaction() == null) {
            faction = "Zivilist";
        }
        String bloodType = "§4" + playerData.getBloodType();
        if (playerData.getBloodType() == null) {
            bloodType = "Nicht gemessen";
        }
        if (playerData.getFirstname() != null) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.PAPER, 1, 0, "§6" + playerData.getFirstname() + " " + playerData.getLastname(), Arrays.asList("§8 ➥ §eGeschlecht§8:§7 " + playerData.getGender().getTranslation(), "§8 ➥ §eFraktion§8:§7 " + faction, "§8 ➥ §eBlutgruppe§8:§7" + bloodType))) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.PAPER, 1, 0, "§cDu benötigst einen Personalausweis.")) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        }
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openBag(player);
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
        inventoryManager.setItem(new CustomItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWUzZDM2YmE4YTI5NjYzZGZkYmVmMTFmOWIyZDExY2FlMzg4Yzc1Nzg0Y2FiYzcwNmRjNjY4OWE4Y2IwYjM1MSJ9fX0=", 1, 0, "§bPremium", Arrays.asList("§8 » §e30 Tage", "§8 » §e10.000 Coins"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                buy(player, "premium_30");
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
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §eRänge", true, true);
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
        inventoryManager.setItem(new CustomItem(4, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§6Guthaben", Arrays.asList("§8 ➥ §e" + utils.toDecimalFormat(playerData.getCoins()) + " Coins"))) {
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
                buy(player, "gameboost_3");
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
                Main.getInstance().playerManager.removeCoins(player, 20000);
                Main.getInstance().playerManager.redeemRank(player, "vip", 30, "days");
                player.closeInventory();
                break;
            case "premium_30":
                if (playerData.getCoins() < 10000) {
                    player.sendMessage(Main.error + "Du hast nicht genug Coins (10.000).");
                    player.closeInventory();
                    return;
                }
                Main.getInstance().playerManager.removeCoins(player, 10000);
                Main.getInstance().playerManager.redeemRank(player, "premium", 30, "days");
                player.closeInventory();
                break;
            case "gold_30":
                if (playerData.getCoins() < 5000) {
                    player.sendMessage(Main.error + "Du hast nicht genug Coins (5.000).");
                    player.closeInventory();
                    return;
                }
                playerManager.removeCoins(player, 5000);
                playerManager.redeemRank(player, "gold", 30, "days");
                player.closeInventory();
                break;
            case "hausslot":
                if (playerData.getCoins() < 4000) {
                    player.sendMessage(Main.error + "Du hast nicht genug Coins (4.000).");
                    player.closeInventory();
                    return;
                }
                playerManager.removeCoins(player, 4000);
                utils.housing.addHausSlot(player);
                player.closeInventory();
                player.sendMessage("§8[§eCoin-Shop§8]§a Du hast einen Hausslot eingelöst!");
                break;
        }
    }
}
