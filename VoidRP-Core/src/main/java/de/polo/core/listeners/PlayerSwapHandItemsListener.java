package de.polo.core.listeners;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.core.location.services.NavigationService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.Main;
import de.polo.core.faction.entity.Faction;
import de.polo.core.game.base.housing.House;
import de.polo.core.player.services.PlayerService;
import de.polo.core.storage.*;
import de.polo.core.game.base.extra.Storage;
import de.polo.core.game.base.vehicle.PlayerVehicleData;
import de.polo.core.game.base.vehicle.Vehicles;
import de.polo.core.game.faction.gangwar.Gangwar;
import de.polo.core.game.faction.gangwar.GangwarUtils;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.manager.ItemManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.utils.enums.Weapon;
import de.polo.core.utils.gameplay.MilitaryDrop;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.*;
import de.polo.core.utils.player.ChatUtils;
import de.polo.core.utils.Event;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.BlockIterator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.polo.core.Main.*;

@Event
public class PlayerSwapHandItemsListener implements Listener {

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
        //Block block = getTargetBlock(player);
        if (!player.isSneaking() || player.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        if (playerService.isCarrying(player)) {
            InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §7Tragen"));
            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PAPER, 1, 0, "§cFrei lassen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerManager.removeTargetFromArmorStand(player);
                    player.closeInventory();
                }
            });
            return;
        }
        PlayerData playerData = playerManager.getPlayerData(player);
        ItemStack stack = player.getInventory().getItemInMainHand();
        if (stack.getType().equals(Material.WRITTEN_BOOK)) {
            BookMeta meta = (BookMeta) stack.getItemMeta();
            if (meta.getAuthor() != null && meta.getAuthor().equalsIgnoreCase("Void News")) {
                InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §eZeitung / Buch"));
                inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.BOOK, 1, 0, "§eBearbeiten")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        ItemStack book = new ItemStack(Material.WRITABLE_BOOK, 1);
                        book.setItemMeta(meta);
                        player.getInventory().addItem(book);
                        player.getInventory().remove(stack);
                        player.closeInventory();
                    }
                });
                return;
            }
        }
        if (playerData.getVariable("job") != null) {
            if (playerData.getVariable("job").toString().equalsIgnoreCase("corpse")) {
                Corpse corpse = Main.getInstance().utils.deathUtil.getNearbyCorpse(player.getLocation(), 3);
                if (corpse != null) {
                    InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8Leiche"));
                    inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.WITHER_SKELETON_SKULL, 1, 0, "§8Leiche aufheben")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            player.closeInventory();
                            Main.getInstance().utils.deathUtil.jobPickupCorpse(corpse, player);
                        }
                    });
                }
                return;
            }
        }
        for (Storages storage : Storages.values()) {
            if (player.getLocation().distance(storage.getLocation()) < 5) {
                Storage s = Storage.getStorageByTypeAndPlayer(StorageType.EXTRA, player, storage);
                if (s == null) {
                    if (storage.isGeworben()) {
                        if (playerManager.getGeworbenCount(player) < storage.getAmount()) {
                            player.sendMessage(Prefix.ERROR + "Du musst mindestens " + storage.getAmount() + " Spieler werben um dieses Lager zu benutzen.");
                            return;
                        }
                    }
                    s = new Storage(StorageType.EXTRA);
                    s.setExtra(storage);
                    s.setPlayer(player.getUniqueId().toString());
                    s.create();
                }
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
                    s.open(player);
                    return;
                }
            }
        }
        if (Main.getInstance().locationManager.getDistanceBetweenCoords(player, "staatsbank") < 5) {
            Main.getInstance().gamePlay.openStaatsbankRaub(player);
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
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §7Bewusstlose Person (" + nearestSkull.getName() + ")"), true, true);
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
                player.sendMessage("§8 ➥ §eNachname§8:§7 " + targetplayerData.getLastname());
                player.sendMessage("§8 ➥ §eGeschlecht§8:§7 " + targetplayerData.getGender().getTranslation());
                player.sendMessage("§8 ➥ §eGeburtsdatum§8:§7 " + formattedDate);
                player.sendMessage(" ");
                player.sendMessage("§8 ➥ §eWohnort§8:§7 " + utils.houseManager.getHouseAccessAsString(targetplayerData));
            }
        });
        int bargeldAmount = targetplayerData.getBargeld() / 4;
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.BOOK, 1, 0, "§ePortmonee", Arrays.asList("§8 ➥ §7" + Utils.toDecimalFormat(bargeldAmount) + "$", "", "§8[§6Linksklick§8]§7 Geld rauben"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                if (targetplayerData.getVariable("robCd") != null) {
                    LocalDateTime robCd = targetplayerData.getVariable("robCd");
                    if (!Utils.getTime().isAfter(robCd)) {
                        player.sendMessage(Prefix.ERROR + "Der Spieler wurde vor kurzem erst ausgeraubt");
                        return;
                    }
                    playerData.setVariable("robCd", null);
                }
                if (targetplayerData.getBargeld() < 1) {
                    player.sendMessage(Prefix.ERROR + targetplayer.getName() + " hat kein Bargeld dabei.");
                    return;
                }
                if (targetplayerData.getBargeld() < bargeldAmount) {
                    return;
                }
                player.sendMessage("§8[§cAusraub§8]§c Du hast " + targetplayer.getName() + " §a" + bargeldAmount + "$§c geklaut.");
                targetplayer.sendMessage("§8[§cAusraub§8]§c " + player.getName() + " hat dir §4" + bargeldAmount + "$§c geklaut.");
                try {
                    playerManager.addMoney(player, bargeldAmount, "Raub von Spieler (" + targetplayer.getName() + ")");
                    playerManager.removeMoney(targetplayer, bargeldAmount, "Raub (" + player.getName() + ")");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                targetplayerData.setVariable("robCd", Utils.getTime().plusMinutes(5));
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
                    Connection connection = Main.getInstance().coreDatabase.getConnection();
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
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §7Fraktionslager"));
        boolean canTakeout = true;
        for (UUID uuid : ServerManager.factionStorageWeaponsTookout) {
            if (player.getUniqueId().equals(uuid)) canTakeout = false;
        }
        Gangwar gangwar = null;
        for (Gangwar gw : Main.getInstance().utils.gangwarUtils.getGangwars()) {
            if (gw.getAttacker().equals(playerData.getFaction()) || gw.getDefender().equals(playerData.getFaction()))
                gangwar = gw;
        }
        if (gangwar != null) {
            inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.OAK_SIGN, 1, 0, "§6Gangwar betreten", Arrays.asList("§8 ➥ §eGebiet§8:§7 " + gangwar.getGangZone().getName(), "§8 ➥ §eZeit§8:§7 " + gangwar.getMinutes() + "m & " + gangwar.getSeconds() + "s verbleibend", "§8 ➥ §eAngreifer§8:§7 " + gangwar.getAttacker() + " (" + gangwar.getAttackerPoints() + ")", "§8 ➥ §eVerteidiger§8:§7 " + gangwar.getDefender() + " (" + gangwar.getDefenderPoints() + ")", "§8 ➥ §ePlätze§8:§7 " + gangwar.getMaxMember()))) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    GangwarUtils gangwarUtils = Main.getInstance().utils.gangwarUtils;
                    FactionManager factionManager = Main.getInstance().factionManager;
                    if (playerData.getFaction() == null || Objects.equals(playerData.getFaction(), "Zivilist")) {
                        player.sendMessage(Prefix.ERROR + "Du bist in keiner Fraktion.");
                        return;
                    }
                    if (playerData.getVariable("gangwar") != null) {
                        player.sendMessage("§8[§cGangwar§8]§c Du bist bereits im Gangwar.");
                        player.closeInventory();
                        return;
                    }
                    Faction factionData = factionManager.getFactionData(playerData.getFaction());
                    if (factionData.getCurrent_gangwar() != null) {
                        if (!gangwarUtils.joinGangwar(player, factionData.getCurrent_gangwar())) return;
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
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cWaffenschrank")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                Main.getInstance().getHouseManager().openGunCabinet(player, new House(0, 0, 0));
            }
        });
        /*inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.VILLAGER_SPAWN_EGG, 1, 0, "§cDealer suchen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openDealers(player);
            }
        });*/
        if (MilitaryDrop.ACTIVE) {
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.OAK_SIGN, 1, 0, "§cMilitärabsturz beitreten")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    if (!Main.getInstance().gamePlay.militaryDrop.handleJoin(player)) {
                        player.sendMessage(Prefix.ERROR + "Du kannst dem Absturz-Event nicht beitreten!");
                    }
                }
            });
        }
    }

    private void openDealers(Player player) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §cDealer"));
        Collection<Dealer> dealers = Main.getInstance().gamePlay.getCurrentDealer();

        List<Integer> slots = Arrays.asList(11, 13, 15);
        int index = 0;
        NavigationService navigationService = VoidAPI.getService(NavigationService.class);
        for (Dealer dealer : dealers.stream().limit(3).collect(Collectors.toList())) {
            int slot = slots.get(index);
            inventoryManager.setItem(new CustomItem(slot, ItemManager.createItem(Material.VILLAGER_SPAWN_EGG, 1, 0, "§cDealer-" + dealer.getGangzone(), "§8 ➥ §e" + (int) player.getLocation().distance(dealer.getLocation()) + "m")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    navigationService.createNaviByCord(player, (int) dealer.getLocation().getX(), (int) dealer.getLocation().getY(), (int) dealer.getLocation().getZ());
                }
            });
            index++;
        }
    }


    private void openBag(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §6Deine Tasche"), true, true);
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.BOOK, 1, 0, "§ePortmonee", "§8 ➥ §7" + Utils.toDecimalFormat(playerData.getBargeld()) + "$")) {
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
        inventoryManager.setItem(new CustomItem(21, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§eCoin-Shop", Collections.singletonList("§8 ➥ §7Ränge, Cosmetics und vieles mehr!"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCoinShop(player, playerData);
            }
        });
        inventoryManager.setItem(new CustomItem(22, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJmYTdiMDA1OGIxOTExOGFkNmQ4MTRlNzNlNTRiM2U3YjRlZWUxNzJkMWJhODBjNDQyOTc2ODgwOGJiNjdhIn19fQ==", 1, 0, "§aTiere")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPetMenu(player, playerData);
            }
        });
        inventoryManager.setItem(new CustomItem(23, ItemManager.createItemHead(player.getUniqueId().toString(), 1, 0, "§bStatistiken", "§8 ➥ §7Übersicht zu Spieler & Character Statistiken")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openStatistics(player, playerData);
            }
        });

        if (playerData.getFaction() != null && playerData.isLeader()) {
            inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.PAPER, 1, 0, "§cLeadermenü")) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        }
    }

    private void openPetMenu(Player player, PlayerData playerData) {
        int i = 0;
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §aTiere"), true, true);
        for (PetStoreItem petStoreItem : PetStoreItem.values()) {
            String priceTag = null;
            if (petStoreItem.getPriceType().equals(PriceType.CASH)) {
                priceTag = "§a" + petStoreItem.getPrice() + "$";
            } else if (petStoreItem.getPriceType().equals(PriceType.VOTES)) {
                priceTag = "§e" + petStoreItem.getPrice() + " Votes";
            } else if (petStoreItem.getPriceType().equals(PriceType.RECRUITED)) {
                priceTag = "§5" + petStoreItem.getPrice() + " Spieler geworben";
            }
            PlayerPed ped = playerData.getPlayerPetManager().getPed(petStoreItem.getPet());
            if (ped == null) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(petStoreItem.getMaterial(), 1, 0, petStoreItem.getPet().getDisplayname(), priceTag)) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        switch (petStoreItem.getPriceType()) {
                            case CASH:
                                if (playerData.getBargeld() < petStoreItem.getPrice()) {
                                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld!");
                                    return;
                                }
                                playerData.removeMoney(petStoreItem.getPrice(), "Kauf von " + petStoreItem.getPet().name());
                                break;
                            case VOTES:
                                if (playerData.getVotes() < petStoreItem.getPrice()) {
                                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Votes!");
                                    return;
                                }
                                playerData.setVotes(playerData.getVotes() - petStoreItem.getPrice());
                                playerData.save();
                                break;
                            case RECRUITED:
                                if (playerManager.getGeworbenCount(player) < petStoreItem.getPrice()) {
                                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Spieler geworben!");
                                    return;
                                }
                                break;
                            default:
                                return;
                        }

                        PlayerPed playerPed = new PlayerPed(petStoreItem.getPet(), false);
                        playerData.getPlayerPetManager().addPet(playerPed, true);
                        openPetMenu(player, playerData);
                    }
                });
            } else {
                String activeState = "§cInaktiv";
                if (ped.isActive()) {
                    activeState = "§aAktiv";
                }
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(petStoreItem.getMaterial(), 1, 0, petStoreItem.getPet().getDisplayname(), "§8 ➥ " + activeState)) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (!ped.isActive()) {
                            if (playerData.getPlayerPetManager().getActivePed() != null) {
                                player.sendMessage(Prefix.ERROR + "Du hast bereits ein Tier ausgewählt.");
                                return;
                            }
                        }
                        playerData.getPlayerPetManager().changeState(ped, !ped.isActive());
                        openPetMenu(player, playerData);
                    }
                });
            }
            i++;
        }
    }

    private void openStatistics(Player player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §eStatistiken"), true, true);
        inventoryManager.setItem(new CustomItem(4, ItemManager.createItemHead(player.getUniqueId().toString(), 1, 0, "§6" + player.getName(), Arrays.asList("§8 ➥ §eLevel§8:§7 " + playerData.getLevel() + " (" + playerData.getExp() + "/" + playerData.getNeeded_exp() + ")", "§8 ➥ §eVisum§8:§7 " + playerData.getVisum() + " (" + playerData.getCurrentHours() + "/" + (playerData.getVisum() * 4) + ")"))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        String faction = playerData.getFaction() + " (" + playerData.getFactionGrade() + "/6)";
        if (playerData.getFaction() == null) {
            faction = "Zivilist";
        }
        String bloodType = "§4" + playerData.getBloodType();
        if (playerData.getBloodType() == null) {
            bloodType = "Nicht gemessen";
        }
        if (playerData.getFirstname() != null) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.PAPER, 1, 0, "§6" + playerData.getFirstname() + " " + playerData.getLastname(), Arrays.asList("§8 ➥ §eGeschlecht§8:§7 " + playerData.getGender().getTranslation(), "§8 ➥ §eFraktion§8: §7 " + faction, "§8 ➥ §eBlutgruppe§8:§7" + bloodType))) {
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

        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.DIAMOND, 1, 0, "§bPowerups")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPowerUpMenu(player);
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openBag(player);
            }
        });
    }

    private void openCoinShop(Player player, PlayerData playerData) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §eCoin-Shop"), true, true);
        inventoryManager.setItem(new CustomItem(4, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§6Guthaben", Collections.singletonList("§8 ➥ §e" + Utils.toDecimalFormat(playerData.getCoins()) + " Coins"))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWUzZDM2YmE4YTI5NjYzZGZkYmVmMTFmOWIyZDExY2FlMzg4Yzc1Nzg0Y2FiYzcwNmRjNjY4OWE4Y2IwYjM1MSJ9fX0=", 1, 0, "§bPremium", Arrays.asList("§8 » §e30 Tage", "§8 » §e10.000 Coins", "", "§8 » §7[§6Rechtsklick§8]§7 Mehr Informationen"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (event.isLeftClick()) {
                    buy(player, "premium_30");
                } else {
                    player.sendMessage("§7   ===§8[§6Premium§8]§7===");
                    player.sendMessage("§8 ➥ §a2x Sozialbonus");
                    player.sendMessage("§8 ➥ §aExtra Spielzeitbelohnungen");
                    player.sendMessage("§8 ➥ §5Labor startet automatisch");
                    player.sendMessage("§8 ➥ §6Business §8(§e/business§8)");
                    player.sendMessage("§8 ➥ §6Spawn nach Despawn ändern §8(§e/changespawn§8)");
                    player.sendMessage("§8 ➥ §6extra TeamSpeak Rolle");
                    player.sendMessage("§8 ➥ §6Visuell in der Tablist §8(§d◈§8)");
                }
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
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §eRänge"), true, true);
        Inventory inv = Bukkit.createInventory(player, 27, "§8 » §eRänge");
        inventoryManager.setItem(new CustomItem(4, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§6Guthaben", Collections.singletonList("§8 ➥ §e" + Utils.toDecimalFormat(playerData.getCoins()) + " Coins"))) {
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
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §2Extras"), true, true);
        inventoryManager.setItem(new CustomItem(4, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZhNzU5OTVjZTUzYmQzNjllZDczNjE1YmYzMjNlMTRhOWNkNzc4OGNhNWFjYjY1YjBiMWFmNTY0NWRkZDA5MSJ9fX0=", 1, 0, "§6Guthaben", Collections.singletonList("§8 ➥ §e" + Utils.toDecimalFormat(playerData.getCoins()) + " Coins"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
            }
        });
        inventoryManager.setItem(new CustomItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmY4MTIxMTJkZDE4N2U3YzhkZGI1YzNiOGU4NTRlODJmMTkxOTc0MTRhOGNkYjU0MjAyMWYxYTQ5MTg5N2U1MyJ9fX0=", 1, 0, "§bHausslot", Collections.singletonList("§8 » §e6.000 Coins"))) {
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
        inventoryManager.setItem(new CustomItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzkyNzRhMmFjNTQxZTQwNGMwYWE4ODg3OWIwYzhiMTBmNTAyYmMyZDdlOWE2MWIzYjRiZjMzNjBiYzE1OTdhMiJ9fX0=", 1, 0, "§6Vortex Munition", Arrays.asList("§8 » §e20 Schuss", "§8 » §e600 Coins"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                buy(player, "vortex-mag");
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCoinShop(player, playerData);
            }
        });
    }

    @SneakyThrows
    private void buy(Player player, String type) {
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player.getUniqueId());
        switch (type) {
            case "vip_30":
                if (playerData.getCoins() < 20000) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Coins (20.000).");
                    player.closeInventory();
                    return;
                }
                Main.getInstance().playerManager.removeCoins(player, 20000);
                Main.getInstance().playerManager.redeemRank(player, "vip", 30, "days");
                player.closeInventory();
                break;
            case "premium_30":
                if (playerData.getCoins() < 10000) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Coins (10.000).");
                    player.closeInventory();
                    return;
                }
                Main.getInstance().playerManager.removeCoins(player, 10000);
                Main.getInstance().playerManager.redeemRank(player, "premium", 30, "days");
                player.closeInventory();
                break;
            case "gold_30":
                if (playerData.getCoins() < 5000) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Coins (5.000).");
                    player.closeInventory();
                    return;
                }
                playerManager.removeCoins(player, 5000);
                playerManager.redeemRank(player, "gold", 30, "days");
                player.closeInventory();
                break;
            case "hausslot":
                if (playerData.getCoins() < 6000) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Coins (6.000).");
                    player.closeInventory();
                    return;
                }
                playerManager.removeCoins(player, 6000);
                utils.houseManager.addHausSlot(player);
                player.closeInventory();
                player.sendMessage("§8[§eCoin-Shop§8]§a Du hast einen Hausslot eingelöst!");
                break;
            case "gameboost_3":
                if (playerData.getCoins() < 2000) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Coins (2.000).");
                    player.closeInventory();
                    return;
                }
                playerManager.removeCoins(player, 2000);
                playerManager.addEXPBoost(player, 3);
                player.closeInventory();
                player.sendMessage("§8[§eCoin-Shop§8]§a Du hast einen EXP-Boost eingelöst!");
                break;
            case "vortex-mag":
                if (playerData.getCoins() < 600) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Coins (600).");
                    player.closeInventory();
                    return;
                }
                playerManager.removeCoins(player, 600);
                Main.getInstance().getWeaponManager().giveWeaponToCabinet(player, Weapon.MARKSMAN, 20, 0);
                player.closeInventory();
                player.sendMessage("§8[§eCoin-Shop§8]§a Du hast einen EXP-Boost eingelöst!");
                break;
        }
    }

    private void openPowerUpMenu(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §bPowerups"));
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.EXPERIENCE_BOTTLE, 1, 0, "§bEXP", Arrays.asList("§8 » §e" + playerData.getPlayerPowerUpManager().getPowerUp(Powerup.EXP).getAmount() + "% §8➡ §6" + (playerData.getPlayerPowerUpManager().getPowerUp(Powerup.EXP).getAmount() + Powerup.EXP.getUpgradeAmount()) + "%", "", "§b » §7Erhöht den EXP-Output", "§8 » §a" + Utils.toDecimalFormat(playerData.getPlayerPowerUpManager().getUpgradePrice(Powerup.EXP)) + "$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!playerData.getPlayerPowerUpManager().upgrade(Powerup.EXP)) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei!");
                    return;
                }
                player.closeInventory();
                player.sendMessage(Prefix.MAIN + "Du hast ein EXP-Upgrade gekauft.");
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§7Steuern", Arrays.asList("§8 » §e" + playerData.getPlayerPowerUpManager().getPowerUp(Powerup.TAX).getAmount() + "% §8➡ §6" + (playerData.getPlayerPowerUpManager().getPowerUp(Powerup.TAX).getAmount() + Powerup.TAX.getUpgradeAmount()) + "%", "", "§b » §7Verringert die höhe der Steuern", "§8 » §a" + Utils.toDecimalFormat(playerData.getPlayerPowerUpManager().getUpgradePrice(Powerup.TAX)) + "$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!playerData.getPlayerPowerUpManager().upgrade(Powerup.TAX)) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei!");
                    return;
                }
                player.closeInventory();
                player.sendMessage(Prefix.MAIN + "Du hast ein Steuer-Upgrade gekauft.");
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.CHEST, 1, 0, "§9Lager", Arrays.asList("§8 » §e" + playerData.getPlayerPowerUpManager().getPowerUp(Powerup.STORAGE).getAmount() + " Slots §8➡ §6" + (playerData.getPlayerPowerUpManager().getPowerUp(Powerup.STORAGE).getAmount() + Powerup.STORAGE.getUpgradeAmount()) + " Slots", "", "§b » §7Erhöht die Anzahl der Slots in allen Lagern", "§8 » §a" + Utils.toDecimalFormat(playerData.getPlayerPowerUpManager().getUpgradePrice(Powerup.STORAGE)) + "$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!playerData.getPlayerPowerUpManager().upgrade(Powerup.STORAGE)) {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei!");
                    return;
                }
                player.closeInventory();
                player.sendMessage(Prefix.MAIN + "Du hast ein Lager-Upgrade gekauft.");
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openStatistics(player, playerData);
            }
        });
    }
}
