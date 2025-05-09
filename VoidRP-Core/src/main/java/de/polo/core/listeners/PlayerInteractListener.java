package de.polo.core.listeners;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.game.base.extra.Storage;
import de.polo.core.game.base.extra.drop.Drop;
import de.polo.core.game.base.housing.House;
import de.polo.core.game.events.SecondTickEvent;
import de.polo.core.jobs.commands.MuellmannCommand;
import de.polo.core.jobs.commands.PostboteCommand;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.ItemManager;
import de.polo.core.manager.WeaponManager;
import de.polo.core.storage.ATM;
import de.polo.core.storage.ClickedEventBlock;
import de.polo.core.faction.entity.Faction;
import de.polo.core.storage.Grenade;
import de.polo.core.storage.Molotov;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.storage.WeaponData;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.CaseType;
import de.polo.core.utils.enums.Drug;
import de.polo.core.utils.enums.RoleplayItem;
import de.polo.core.utils.enums.StorageType;
import de.polo.core.utils.gameplay.Case;
import de.polo.core.utils.gameplay.GamePlay;
import de.polo.core.utils.player.ChatUtils;
import de.polo.core.utils.Event;
import de.polo.core.utils.player.PlayerPacket;
import de.polo.core.utils.player.Rubbellose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import static de.polo.core.Main.*;

@Event
public class PlayerInteractListener implements Listener {
    private static final Random RANDOM = new Random();
    private final HashMap<Player, LocalDateTime> rammingPlayers = new HashMap<>();

    private final List<Molotov> molotovs = new ObjectArrayList<>();
    private final List<Block> sprungtuecher = new ObjectArrayList<>();
    private final List<Grenade> activeGrenades = new ObjectArrayList<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
        playerData.setIntVariable("afk", 0);
        if (player.getGameMode().equals(GameMode.SPECTATOR)) return;
        if (event.getHand() != null && event.getHand().equals(org.bukkit.inventory.EquipmentSlot.OFF_HAND)) {
            event.setCancelled(true);
            return;
        }
        if (playerData.isAFK()) {
            utils.setAFK(player, false);
            PlayerPacket packet = new PlayerPacket(player);
            packet.renewPacket();
        }
        if (playerData.isDead()) {
            event.setCancelled(true);
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null) {
                if (event.getClickedBlock().getType() == Material.OAK_STAIRS || event.getClickedBlock().getType() == Material.ACACIA_STAIRS || event.getClickedBlock().getType() == Material.SPRUCE_STAIRS || event.getClickedBlock().getType() == Material.BIRCH_STAIRS || event.getClickedBlock().getType() == Material.JUNGLE_STAIRS || event.getClickedBlock().getType() == Material.DARK_OAK_STAIRS) {
                    if (player.getGameMode().equals(GameMode.CREATIVE)) return;
                    for (WeaponData weaponData : WeaponManager.weaponDataMap.values()) {
                        if (player.getInventory().getItemInMainHand().getType() == weaponData.getMaterial()) {
                            return;
                        }
                    }
                    Location l = event.getClickedBlock().getLocation();
                    World w = player.getWorld();
                    ArmorStand armorStand = w.spawn(l.add(0.5D, -1, 0.5D), ArmorStand.class);
                    armorStand.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "id"), PersistentDataType.INTEGER, 0);
                    armorStand.setVisible(false);
                    armorStand.addPassenger(player);
                }
                if (event.getClickedBlock().getType().equals(Material.PLAYER_HEAD)) {
                    RegisteredBlock block = blockManager.getBlockAtLocation(event.getClickedBlock().getLocation());
                    // ISSUE VRP-10001: Null check for block
                    if (block == null || block.getInfo() == null || block.getInfoValue() == null) return;
                    if (!playerData.addClickedBlock(block)) {
                        utils.sendActionBar(player, "§cDieses Geschenk hast du bereits gefunden! (" + playerData.getClickedEventBlocks().size() + "/25)");
                        return;
                    }
                    int found = 0;
                    for (ClickedEventBlock eventBlock : playerData.getClickedEventBlocks()) {
                        RegisteredBlock b = blockManager.getBlockById(eventBlock.getBlockId());
                        if (b.getInfoValue().equalsIgnoreCase(block.getInfoValue())) found++;
                    }
                    int total = 25;
                    playerData.addMoney(150, "Geschenk");
                    utils.sendActionBar(player, "§aDu hast ein Geschenk gefunden! (" + found + "/" + total + ")");
                    if (found >= total) {
                        playerData.setVariable("event::endTime", Utils.getTime());
                        long diff = Duration.between(playerData.getVariable("event::startTime"), playerData.getVariable("event::endTime")).toSeconds();
                        player.sendMessage("§8[§cWeihnachten§8]§a Du hast alle Geschenke in " + Utils.getTime((int) diff) + " gefunden!");
                        playerManager.addExp(player, Utils.random(100, 200));
                    }
                }
                if (event.getClickedBlock().getType().toString().contains("BANNER")) {
                    RegisteredBlock block = blockManager.getBlockAtLocation(event.getClickedBlock().getLocation());
                    if (block != null && block.getInfo() != null && block.getInfoValue() != null) {

                        if (block.getInfo().equalsIgnoreCase("banner")) {
                            if (playerData.getFaction() == null) return;
                            InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §bBanner " + block.getInfoValue()));
                            Block b = event.getClickedBlock();
                            Faction factionData = factionManager.getFactionData(playerData.getFaction());
                            if (!factionManager.isBannerRegistered(block)) {
                                factionManager.updateBanner(block, factionData);
                            }
                            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.WHITE_BANNER, 1, 0, "§cÜbersprühen" + (factionManager.canSprayBanner(block) ? "" : " §8(§cx§8)"))) {
                                @Override
                                public void onClick(InventoryClickEvent event) {
                                    player.closeInventory();
                                    if (!factionManager.canSprayBanner(block)) {
                                        return;
                                    }
                                    BlockState state = b.getState();
                                    /*BlockData blockData = state.getBlockData();

                                    BlockFace originalFace = null;
                                    boolean isWallBanner = false;
                                    if (blockData instanceof Directional) {
                                        isWallBanner = true;
                                        Directional directional = (Directional) blockData;
                                        originalFace = directional.getFacing();
                                        System.out.println("Original facing: " + originalFace);
                                    }*/

                                    Banner banner = (Banner) state;
                                    //banner.setType(factionData.getBannerColor());
                                    banner.setPatterns(factionData.getBannerPattern());
                                    banner.update(true, true);

                                    /*if (isWallBanner) {
                                        Directional newDirectional = (Directional) b.getBlockData();
                                        newDirectional.setFacing(originalFace);
                                        b.setBlockData(newDirectional);
                                        state.update(true, true);
                                        System.out.println("New facing: " + newDirectional.getFacing());
                                    }*/

                                    player.sendMessage("§8[§bBanner§8]§7 Du hast den Banner §3" + block.getInfoValue() + "§7 übersprüht!");
                                    factionManager.updateBanner(block, factionData);
                                    playerManager.addExp(player, Utils.random(5, 10));
                                }
                            });
                        }
                    }
                }
                if (event.getClickedBlock().getType() == Material.OAK_DOOR) {
                    RegisteredBlock rBlock = blockManager.getBlockAtLocation(event.getClickedBlock().getLocation());
                    if (rBlock != null && rBlock.getInfo() != null) {
                        if (rBlock.getInfo().equalsIgnoreCase("fdoor")) {
                            if (playerData.getFaction() == null) {
                                event.setCancelled(true);
                                return;
                            }

                            String playerFaction = playerData.getFaction().toLowerCase();
                            String blockFaction = rBlock.getInfoValue().toLowerCase();

                            if (!voidPlayer.isAduty() && !playerFaction.equals(blockFaction)) {
                                if (!((blockFaction.equals("fbi") && playerFaction.equals("polizei")) ||
                                        (blockFaction.equals("polizei") && playerFaction.equals("fbi")))) {
                                    event.setCancelled(true);
                                    if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
                                        System.out.println(blockFaction);
                                        Faction defender = factionManager.getFactionData(blockFaction);
                                        Main.getInstance().gamePlay.openGOVRaidGUI(defender, player);
                                    }
                                }
                            }
                        }
                    }
                    int centerX = event.getClickedBlock().getLocation().getBlockX();
                    int centerY = event.getClickedBlock().getLocation().getBlockY();
                    int centerZ = event.getClickedBlock().getLocation().getBlockZ();
                    World world = event.getClickedBlock().getWorld();
                    for (int x = centerX - 3; x <= centerX + 3; x++) {
                        for (int y = centerY - 3; y <= centerY + 3; y++) {
                            for (int z = centerZ - 3; z <= centerZ + 3; z++) {
                                Location location = new Location(world, x, y, z);
                                Block block = location.getBlock();
                                if (block.getType().toString().contains("SIGN")) {
                                    RegisteredBlock registeredBlock = blockManager.getBlockAtLocation(block.getLocation());
                                    if (registeredBlock.getInfoValue() == null) continue;
                                    if (!voidPlayer.isAduty() && !utils.houseManager.canPlayerInteract(player, Integer.parseInt(registeredBlock.getInfoValue()))) {
                                        event.setCancelled(true);
                                    }
                                }
                            }
                        }
                    }
                }
                if (event.getClickedBlock().getType() == Material.CHEST) {
                    RegisteredBlock registeredBlock = blockManager.getBlockAtLocation(event.getClickedBlock().getLocation());
                    if (registeredBlock != null) {
                        if (registeredBlock.getInfoValue() != null && registeredBlock.getInfo() != null && playerData.getFaction() != null) {
                            if (registeredBlock.getInfo().equalsIgnoreCase("storage") && registeredBlock.getInfoValue().equalsIgnoreCase(playerData.getFaction())) {
                                event.setCancelled(false);
                            }
                        }
                    }
                    if (Main.getInstance().gamePlay.activeDrop != null) {
                        Drop drop = Main.getInstance().gamePlay.activeDrop;
                        if (drop.location.distance(event.getClickedBlock().getLocation()) < 1) {
                            if (!drop.isDropOpen) {
                                player.sendMessage("§8[§cDrop§8]§7 Der drop ist noch nicht offen!");
                                event.setCancelled(true);
                                return;
                            } else {
                                if (event.getClickedBlock().getState() instanceof Chest && drop.isDropOpen) {
                                    drop.open(player);
                                }
                            }
                        }
                    }
                    if (playerData.getFaction().equalsIgnoreCase("Polizei") || playerData.getFaction().equalsIgnoreCase("FBI")) {
                        LocationService locationService = VoidAPI.getService(LocationService.class);
                        if (locationService.getDistanceBetweenCoords(player, "asservatenkammer") < 20) {
                            Main.getInstance().gamePlay.drugstorage.openEvidence(player);
                        }
                    }
                    if (registeredBlock != null && registeredBlock.getInfo() != null && registeredBlock.getInfo().equalsIgnoreCase("dlager")) {
                        if (registeredBlock.getInfoValue() != null && registeredBlock.getInfoValue().equalsIgnoreCase(playerData.getFaction())) {
                            Main.getInstance().gamePlay.drugstorage.open(player);
                        }
                    }
                }
                RegisteredBlock factionBlock = blockManager.getBlockAtLocation(event.getClickedBlock().getLocation());
                if (factionBlock != null) {
                    if (factionBlock.getInfo().equalsIgnoreCase("factionupgrade")) {
                        System.out.println("INFO: " + factionBlock.getInfo());
                        if (factionBlock.getInfoValue().equalsIgnoreCase(playerData.getFaction())) {
                            System.out.println("INFOVAL: " + factionBlock.getInfoValue());
                            Main.getInstance().gamePlay.factionUpgradeGUI.open(player);
                        }
                    }
                }
                if (event.getClickedBlock().getType() == Material.CAULDRON) {
                    Material[] items = {Material.POTATO, Material.POISONOUS_POTATO, Material.GLASS_BOTTLE};
                    if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "mülleimer")) {
                        Main.getInstance().getCooldownManager().setCooldown(player, "mülleimer", 30);
                        InventoryManager inventoryManager = new InventoryManager(player, 9, Component.text("§7Mülleimer"), false, false);
                        for (int i = 0; i < Utils.random(3, 4); i++) {
                            inventoryManager.setItem(new CustomItem(Utils.random(0, 8), ItemManager.createItem(items[new Random().nextInt(items.length)], 1, 0, new ItemStack(items[new Random().nextInt(items.length)]).getItemMeta().getDisplayName())) {
                                @Override
                                public void onClick(InventoryClickEvent event) {

                                }
                            });
                        }
                        Main.getInstance().seasonpass.didQuest(player, 10);
                        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " durchwühlt einen Mülleimer.");
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0);
                    } else {
                        String actionBarText = "§7Warte noch " + Main.getInstance().getCooldownManager().getRemainingTime(player, "mülleimer") + " Sekunden!";
                        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
                    }
                }
                if (event.getClickedBlock().getState() instanceof TileState) {
                    TileState state = (TileState) event.getClickedBlock().getState();
                    if (state instanceof Sign) {
                        event.setCancelled(true);
                        Sign sign = (Sign) event.getClickedBlock().getState();
                        if (sign.getLine(1).contains("Bankautomat")) {
                            for (ATM atm : utils.bankingUtils.getATMs()) {
                                if (atm.getLocation().getX() == sign.getLocation().getX()
                                        && atm.getLocation().getY() == sign.getLocation().getY()
                                        && atm.getLocation().getZ() == sign.getLocation().getZ()) {
                                    utils.bankingUtils.openBankMenu(player, atm);
                                    return;
                                }
                            }
                            player.sendMessage(Prefix.ERROR + "Dieser Automat wurde noch nicht registriert.");
                        }
                        RegisteredBlock block = blockManager.getBlockAtLocation(event.getClickedBlock().getLocation());
                        if (block != null && block.getInfo() != null && Objects.equals(block.getInfo(), "house")) {
                            House houseData = utils.houseManager.getHouse(Integer.parseInt(block.getInfoValue()));
                            // ISSUE VRP-10001: Null check for houseData
                            if (houseData != null) {
                                playerData.setIntVariable("current_house", houseData.getNumber());
                                InventoryManager inventoryManager = new InventoryManager(player, 45, Component.text("§8 » §7Haus " + houseData.getNumber()), true, true);
                                if (houseData.getOwner() != null) {
                                    OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(houseData.getOwner()));
                                    inventoryManager.setItem(new CustomItem(13, ItemManager.createItemHead(houseData.getOwner(), 1, 0, "§6Besitzer", "§8 ➥ §7" + owner.getName())) {
                                        @Override
                                        public void onClick(InventoryClickEvent event) {

                                        }
                                    });
                                    if (Main.getInstance().getHouseManager().canPlayerInteract(player, houseData.getNumber())) {
                                        inventoryManager.setItem(new CustomItem(39, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cWaffenschrank öffnen", "§8 ➥ §7" + playerData.getWeapons().size() + " Waffen")) {
                                            @Override
                                            public void onClick(InventoryClickEvent event) {
                                                Main.getInstance().getHouseManager().openGunCabinet(player, houseData);
                                            }
                                        });
                                    }
                                    if (houseData.getOwner().equals(player.getUniqueId().toString())) {
                                        if (Main.getInstance().getHouseManager().canPlayerInteract(player, houseData.getNumber())) {
                                            inventoryManager.setItem(new CustomItem(41, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§6Hauskasse öffnen", "§8 ➥ §a" + Utils.toDecimalFormat(houseData.getMoney()) + "$")) {
                                                @Override
                                                public void onClick(InventoryClickEvent event) {
                                                    Main.getInstance().getHouseManager().openHouseTreasury(player, houseData);
                                                }
                                            });
                                        }
                                        inventoryManager.setItem(new CustomItem(33, ItemManager.createItem(Material.RED_DYE, 1, 0, "§cHaus verkaufen", "§8 ➥§7 Du erhälst: " + new DecimalFormat("#,###").format(houseData.getPrice() * 0.8) + "$")) {
                                            @Override
                                            public void onClick(InventoryClickEvent event) {
                                                // House houseData = Housing.houseDataMap.get(playerData.getIntVariable("current_house"));
                                                if (utils.houseManager.resetHouse(houseData.getNumber())) {
                                                    playerData.addMoney((int) (houseData.getPrice() * 0.8), "Haus-Verkauf (" + houseData.getNumber() + ")");
                                                    player.sendMessage("§8[§6Haus§8]§a Du hast Haus " + houseData.getNumber() + " für " + (int) (houseData.getPrice() * 0.8) + "$ verkauft.");
                                                    player.closeInventory();
                                                }
                                            }
                                        });
                                        inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.CHEST, 1, 0, "§7Lager öffnen")) {
                                            @Override
                                            public void onClick(InventoryClickEvent event) {
                                                Storage s = Storage.getStorageByTypeAndPlayer(StorageType.HOUSE, player, houseData.getNumber());
                                                if (s == null) {
                                                    s = new Storage(StorageType.HOUSE);
                                                    s.setPlayer(player.getUniqueId().toString());
                                                    s.setHouseNumber(houseData.getNumber());
                                                    s.create();
                                                }
                                                s.open(player);
                                            }
                                        });
                                        inventoryManager.setItem(new CustomItem(21, ItemManager.createItem(Material.IRON_BLOCK, 1, 0, "§7Server-Raum" + (!houseData.isServerRoom() ? " §8[§cNicht ausgebaut§8]" : ""))) {
                                            @Override
                                            public void onClick(InventoryClickEvent event) {
                                                if (!houseData.isServerRoom()) return;
                                                Main.getInstance().houseManager.openHouseServerRoom(player, houseData);
                                            }
                                        });
                                    } else {
                                        inventoryManager.setItem(new CustomItem(33, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§c§mHaus verkaufen", "§8 ➥§7 Dieses Haus gehört dir nicht.")) {
                                            @Override
                                            public void onClick(InventoryClickEvent event) {

                                            }
                                        });
                                    }
                                } else {
                                    inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.SKELETON_SKULL, 1, 0, "§7Kein Besitzer")) {
                                        @Override
                                        public void onClick(InventoryClickEvent event) {

                                        }
                                    });
                                    inventoryManager.setItem(new CustomItem(33, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aHaus kaufen", "§8 ➥§e " + new DecimalFormat("#,###").format(houseData.getPrice()) + "$")) {
                                        @Override
                                        public void onClick(InventoryClickEvent event) {
                                            player.closeInventory();
                                            player.performCommand("buyhouse " + playerData.getIntVariable("current_house"));
                                        }
                                    });
                                }
                                inventoryManager.setItem(new CustomItem(29, ItemManager.createItem(Material.PAPER, 1, 0, "§bInformationen", Arrays.asList("§8 ➥ §ePreis§8:§7 " + new DecimalFormat("#,###").format(houseData.getPrice()) + "$", "§8 ➥ §eUmsatz§8: §7" + new DecimalFormat("#,###").format(houseData.getTotalMoney()) + "$", "§8 ➥ §eMieterslots§8:§7 " + houseData.getTotalSlots()))) {
                                    @Override
                                    public void onClick(InventoryClickEvent event) {

                                    }
                                });
                            /*if (houseData.getHouseType().isCanCook()) {
                                inventoryManager.setItem(new CustomItem(39, ItemManager.createItem(Material.FLINT_AND_STEEL, 1, 0, "§bKochen")) {
                                    @Override
                                    public void onClick(InventoryClickEvent event) {
                                        Main.getInstance().housing.openCookMenu(player, houseData);
                                    }
                                });
                            }*/
                                if (voidPlayer.getActiveJob() == null) {
                                    if (playerData.getFaction() != null && (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) && playerData.isDuty()) {
                                        if (rammingPlayers.get(player) != null) {
                                            inventoryManager.setItem(new CustomItem(31, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§9§mRammen", "§8 ➥§7 Du bist bereits eine Tür am eintreten")) {
                                                @Override
                                                public void onClick(InventoryClickEvent event) {

                                                }
                                            });
                                        } else {
                                            inventoryManager.setItem(new CustomItem(31, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§9Rammen", "§8 ➥§7 Fang an die Tür aufzutreten (2 Polizisten benötigt)")) {
                                                @Override
                                                public void onClick(InventoryClickEvent event) {
                                                    Player player = (Player) event.getWhoClicked(); // Ensure we get the player from the event
                                                    List<Player> nearPlayers = new ObjectArrayList<>();
                                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                                        if (p.getWorld() != player.getWorld()) continue;
                                                        PlayerData pData = playerManager.getPlayerData(p);
                                                        if (pData == null || pData.getFaction() == null) continue;
                                                        if (!pData.isExecutiveFaction() || !pData.isDuty()) continue;
                                                        if (player.getLocation().distance(p.getLocation()) < 5 && p != player) {
                                                            nearPlayers.add(p);
                                                        }
                                                    }
                                                    if (nearPlayers.size() < 1) {
                                                        player.sendMessage(Prefix.ERROR + "Es sind nicht genug Polizisten in der Nähe.");
                                                        return;
                                                    }

                                                    double radius = 5.0;
                                                    Block nearestDoorBlock = null;
                                                    double nearestDistance = radius;
                                                    Location playerLocation = player.getLocation();

                                                    for (Block block : blockManager.getNearbyBlocks(playerLocation, radius)) {
                                                        Material type = block.getType();
                                                        if (type == Material.IRON_DOOR || type == Material.OAK_DOOR) {
                                                            double distance = playerLocation.distance(block.getLocation());
                                                            if (distance < nearestDistance) {
                                                                nearestDistance = distance;
                                                                nearestDoorBlock = block;
                                                            }
                                                        }
                                                    }

                                                    if (nearestDoorBlock == null) {
                                                        player.sendMessage(Prefix.ERROR + "Keine Tür in der Nähe gefunden.");
                                                        return;
                                                    }

                                                    BlockState state = nearestDoorBlock.getState();
                                                    if (state.getBlockData() instanceof Openable) {
                                                        Openable openable = (Openable) state.getBlockData();
                                                        if (openable.isOpen()) {
                                                            player.sendMessage(Prefix.ERROR + "Die Tür ist bereits geöffnet.");
                                                        } else {
                                                            openable.setOpen(true);
                                                            state.setBlockData(openable);
                                                            state.update();
                                                            player.sendMessage(Prefix.MAIN + "Die Tür wurde geöffnet.");
                                                        }
                                                    } else {
                                                        player.sendMessage(Prefix.ERROR + "Dies ist keine Tür.");
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        inventoryManager.setItem(new CustomItem(31, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§7Kein Job", "§8 ➥§7 Du hast keinen passenden Job angenommen")) {
                                            @Override
                                            public void onClick(InventoryClickEvent event) {

                                            }
                                        });
                                    }
                                } else {
                                    if (!voidPlayer.getMiniJob().equals(MiniJob.POSTMAN) && !voidPlayer.getMiniJob().equals(MiniJob.WASTE_COLLECTOR)) {
                                        inventoryManager.setItem(new CustomItem(31, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§7Kein Job", "§8 ➥§7 Du hast keinen passenden Job angenommen")) {
                                            @Override
                                            public void onClick(InventoryClickEvent event) {

                                            }
                                        });
                                    } else if (voidPlayer.getMiniJob().equals(MiniJob.POSTMAN)) {
                                        if (Main.getInstance().getCommandInstance(PostboteCommand.class).canGive(houseData.getNumber())) {
                                            inventoryManager.setItem(new CustomItem(31, ItemManager.createItem(Material.BOOK, 1, 0, "§ePost abgeben")) {
                                                @Override
                                                public void onClick(InventoryClickEvent event) {
                                                    Main.getInstance().getCommandInstance(PostboteCommand.class).handleDrop(VoidAPI.getPlayer(player), playerData.getIntVariable("current_house"));
                                                    player.closeInventory();
                                                }
                                            });
                                        } else {
                                            inventoryManager.setItem(new CustomItem(31, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§7Haus bereits beliefert")) {
                                                @Override
                                                public void onClick(InventoryClickEvent event) {

                                                }
                                            });
                                        }
                                    } else if (voidPlayer.getMiniJob().equals(MiniJob.WASTE_COLLECTOR)) {
                                        if (Main.getInstance().getCommandInstance(MuellmannCommand.class).canGet(houseData.getNumber())) {
                                            inventoryManager.setItem(new CustomItem(31, ItemManager.createItem(Material.CAULDRON, 1, 0, "§bMüll einsammeln")) {
                                                @Override
                                                public void onClick(InventoryClickEvent event) {
                                                    Main.getInstance().getCommandInstance(MuellmannCommand.class).handleDrop(VoidAPI.getPlayer(player), playerData.getIntVariable("current_house"));
                                                    player.closeInventory();
                                                }
                                            });
                                        } else {
                                            inventoryManager.setItem(new CustomItem(31, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§7Haus bereits geleert")) {
                                                @Override
                                                public void onClick(InventoryClickEvent event) {

                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            //hier die items und nicht die blöcke


            if (event.getItem() == null) return;
            for (Drug drug : Drug.values()) {
                if (event.getItem().getType().equals(drug.getItem().getMaterial()) && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(drug.getItem().getDisplayName())) {
                    GamePlay.useDrug(player, drug);
                    return;
                }
            }
            if (event.getItem().getItemMeta().getDisplayName().equals("§6§lRubbellos") && event.getItem().getType().equals(Material.PAPER)) {
                Rubbellose rubbellose = new Rubbellose(Main.getInstance().playerManager);
                rubbellose.startGame(player);
                ItemStack itemStack = event.getItem();
                itemStack.setAmount(itemStack.getAmount() - 1);
                if (itemStack.getAmount() >= 1) player.getInventory().setItemInMainHand(itemStack);
            } else if (event.getItem().getItemMeta().getDisplayName().contains("XP-Case")) {
                ItemStack itemStack = event.getItem();
                itemStack.setAmount(itemStack.getAmount() - 1);
                if (itemStack.getAmount() >= 1) player.getInventory().setItemInMainHand(itemStack);
                playerManager.addExp(player, Utils.random(50, 100));
            } else if (event.getItem().getItemMeta().getDisplayName().equals(RoleplayItem.PIPE.getDisplayName())) {
                InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text(""), true, true);
                inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(RoleplayItem.BOX_WITH_JOINTS.getMaterial(), 1, 0, RoleplayItem.BOX_WITH_JOINTS.getDisplayName(), "§8 ➥ §aVerpacke 3 Joints in einer Kiste.")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (ItemManager.getCustomItemCount(player, RoleplayItem.PIPE) < 3) {
                            player.sendMessage(Prefix.ERROR + "Du hast nicht genug Joints.");
                            return;
                        }
                        ItemManager.removeCustomItem(player, RoleplayItem.PIPE, 3);
                        ItemManager.addCustomItem(player, RoleplayItem.BOX_WITH_JOINTS, 1);
                        player.sendMessage("§7Du hast eine Kiste hergestellt.");
                        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " stellt eine Kiste mit Joints her");
                        player.closeInventory();
                    }
                });
            } else if (event.getItem().getItemMeta().getDisplayName().equals("§eMuschel")) {
                InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text(""), true, true);
                inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.BIRCH_BUTTON, 1, 0, "§eMuschel öffnen")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (ItemManager.getItem(player, Material.BIRCH_BUTTON) < 1) {
                            player.sendMessage(Prefix.ERROR + "Du hast keine Muschel dabei.");
                            return;
                        }
                        double randomNumber = Math.random() * 100;
                        ItemManager.removeItem(player, Material.BIRCH_BUTTON, 1);
                        player.sendMessage("§7Du hast eine Muschel geöffnet und erhalten:");
                        player.closeInventory();
                        if (randomNumber < 50) {
                            player.sendMessage("§7 - Nichts");
                        } else if (randomNumber < 70) {
                            player.sendMessage("§7 - Perle");
                            ItemStack perle = ItemManager.createItem(Material.GHAST_TEAR, 1, 0, "§bPerle");
                            player.getInventory().addItem(perle);
                        } else if (randomNumber < 94) {
                            player.sendMessage("§7 - §bDiamant");
                            ItemStack diamond = ItemManager.createItem(Material.DIAMOND, 1, 0, "§bDiamant");
                            player.getInventory().addItem(diamond);
                        }
                    }
                });
                inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.BIRCH_BUTTON, 64, 0, "§eAlle Muscheln öffnen")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (ItemManager.getItem(player, Material.BIRCH_BUTTON) < 1) {
                            player.sendMessage(Prefix.ERROR + "Du hast keine Muschel dabei.");
                            return;
                        }
                        int itemCount = ItemManager.getItem(player, Material.BIRCH_BUTTON);
                        int perlen = 0;
                        int diamanten = 0;
                        ItemManager.removeItem(player, Material.BIRCH_BUTTON, itemCount);
                        player.sendMessage("§7Du hast eine Muschel geöffnet und erhalten:");
                        player.closeInventory();
                        for (int i = 0; i < itemCount; i++) {
                            double randomNumber = Math.random() * 100;
                            if (randomNumber < 30) {
                                perlen++;
                            } else if (randomNumber < 36) {
                                diamanten++;
                            }
                        }
                        if (perlen >= 1) {
                            player.sendMessage("§7 - " + perlen + "x Perlen");
                            ItemManager.addItem(player, Material.GHAST_TEAR, "§bPerle", perlen);
                        }
                        if (diamanten >= 1) {
                            player.sendMessage("§7 - §b" + diamanten + "x Diamanten");
                            ItemManager.addItem(player, Material.DIAMOND, "§bDiamant", diamanten);
                        }
                    }
                });
            } else {
                for (CaseType caseType : CaseType.values()) {
                    if (event.getItem().getItemMeta().getDisplayName().equals(caseType.getDisplayName())) {
                        ItemStack itemStack = event.getItem();
                        itemStack.setAmount(itemStack.getAmount() - 1);
                        if (itemStack.getAmount() >= 1) player.getInventory().setItemInMainHand(itemStack);
                        new Case(player, caseType);
                    }
                }
            }
        }


        /*
        No null
         */


        if (event.getItem() == null) return;
        if (event.getItem().getType().equals(RoleplayItem.MASK.getMaterial()) && Objects.requireNonNull(event.getItem().getItemMeta()).getDisplayName().equalsIgnoreCase(RoleplayItem.MASK.getDisplayName())) {
            if (ItemManager.getCustomItemCount(player, RoleplayItem.MASK) < 1) {
                return;
            }
            ItemManager.removeCustomItem(player, RoleplayItem.MASK, 1);
            Main.getInstance().gamePlay.setMaskState(player, Utils.getTime().plusMinutes(20));
        }

        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            if (ItemManager.equals(player.getInventory().getItemInMainHand(), RoleplayItem.MOLOTOV)) {
                throwMolotov(player);
                ItemManager.removeCustomItem(player, RoleplayItem.MOLOTOV, 1);
            }

            if (ItemManager.equals(player.getInventory().getItemInMainHand(), RoleplayItem.FEUERLÖSCHER)) {
                Vector direction = player.getLocation().getDirection().normalize();
                Location startLocation = player.getEyeLocation();

                int particleCount = 20;
                double offsetX = 0.1;
                double offsetY = 0.1;
                double offsetZ = 0.1;

                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.WHITE, 1.0F);

                for (int i = 1; i <= 5; i++) {
                    Location particleLocation = startLocation.clone().add(direction.clone().multiply(i));

                    player.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, particleCount, offsetX, offsetY, offsetZ, 0.0, dustOptions);

                    Block block = particleLocation.getBlock();
                    if (block.getType().equals(Material.FIRE)) {
                        block.setType(Material.AIR);
                    }
                }
            }

            if (ItemManager.equals(player.getInventory().getItemInMainHand(), RoleplayItem.PFEFFERSPRAY)) {
                Vector direction = player.getLocation().getDirection().normalize();
                Location startLocation = player.getEyeLocation();

                int particleCount = 5;
                double offsetX = 0.1;
                double offsetY = 0.1;
                double offsetZ = 0.1;

                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.GRAY, 1.0F);

                for (int i = 1; i <= 5; i++) {
                    Location particleLocation = startLocation.clone().add(direction.clone().multiply(i));

                    player.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, particleCount, offsetX, offsetY, offsetZ, 0.0, dustOptions);

                    for (Entity entity : player.getWorld().getNearbyEntities(particleLocation, 0.5, 0.5, 0.5)) {
                        if (entity instanceof Player && entity != player) {
                            Player hitPlayer = (Player) entity;
                            hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                            hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 2));
                            break;
                        }
                    }
                }
            }
        }

        if (action == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock != null) {
                if (event.getItem() != null && ItemManager.equals(player.getInventory().getItemInMainHand(), RoleplayItem.SPRUNGTUCH)) {
                    if (event.getBlockFace() == BlockFace.UP) {
                        Block blockAbove = clickedBlock.getRelative(0, 1, 0);

                        if (clickedBlock.getType() == Material.SLIME_BLOCK) {
                            clickedBlock.setType(Material.AIR);
                            sprungtuecher.remove(clickedBlock);
                        } else if (blockAbove.getType() == Material.AIR) {
                            blockAbove.setType(Material.SLIME_BLOCK);
                            sprungtuecher.add(blockAbove);
                        }
                    }
                }
                /*if (event.getItem() != null && ItemManager.equals(player.getInventory().getItemInMainHand(), RoleplayItem.ROADBLOCK)) {
                    if (event.getBlockFace() == BlockFace.UP) {
                        Block blockAbove = clickedBlock.getRelative(0, 1, 0);

                        if (clickedBlock.getType() == Material.STONE_BRICK_WALL) {
                            clickedBlock.setType(Material.AIR);
                            Main.getInstance().gamePlay.roadblocks.remove(clickedBlock);
                        } else if (blockAbove.getType() == Material.AIR) {
                            blockAbove.setType(Material.STONE_BRICK_WALL);
                            Main.getInstance().gamePlay.roadblocks.add(blockAbove);
                        }
                    }
                }*/
            }
        }

        if (action == Action.RIGHT_CLICK_AIR && ItemManager.equals(player.getInventory().getItemInMainHand(), RoleplayItem.GRANATE)) {
            throwGrenade(player);
            ItemManager.removeCustomItem(player, RoleplayItem.GRANATE, 1);
        }

        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (playerData.getFaction() != null) {
                if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
                    Block clickedBlock = event.getClickedBlock();
                    if (clickedBlock.getType().equals(RoleplayItem.SPRENGSTOFF.getMaterial())) {
                        Main.getInstance().gamePlay.openBombGUI(player);
                    }
                }
            }
        }
    }

    private void throwGrenade(Player player) {
        ItemStack grenadeItem = new ItemStack(Material.FIRE_CHARGE);
        Item droppedItem = player.getWorld().dropItem(player.getEyeLocation(), grenadeItem);
        droppedItem.setVelocity(player.getLocation().getDirection().multiply(1.5));
        droppedItem.setPickupDelay(Integer.MAX_VALUE);

        activeGrenades.add(new Grenade(Utils.getTime(), droppedItem));
    }

    private void throwMolotov(Player player) {
        ItemStack molotovItem = new ItemStack(Material.FLINT);
        Item droppedItem = player.getWorld().dropItem(player.getEyeLocation(), molotovItem);
        droppedItem.setVelocity(player.getLocation().getDirection().multiply(1.5));
        droppedItem.setPickupDelay(Integer.MAX_VALUE);

        molotovs.add(new Molotov(Utils.getTime(), droppedItem));
    }

    private void checkGrenades() {
        Iterator<Grenade> iterator = activeGrenades.iterator();

        while (iterator.hasNext()) {
            Grenade grenade = iterator.next();
            LocalDateTime thrownTime = grenade.getThrownTime();
            LocalDateTime now = Utils.getTime();

            if (thrownTime.plusSeconds(2).isBefore(now)) {
                Location loc = grenade.getDroppedItem().getLocation();
                Bukkit.getLogger().info("Grenade exploding at: " + now);

                loc.getWorld().createExplosion(loc, 4.0f, false, false);
                grenade.getDroppedItem().remove();
                iterator.remove();
            }
        }
    }

    private void checkMolotovs() {
        Iterator<Molotov> iterator = molotovs.iterator();

        while (iterator.hasNext()) {
            Molotov molotov = iterator.next();
            LocalDateTime thrownTime = molotov.getThrownTime();
            LocalDateTime now = Utils.getTime();

            if (thrownTime.plusSeconds(2).isBefore(now)) {
                Location loc = molotov.getDroppedItem().getLocation();
                Bukkit.getLogger().info("Molotov igniting at: " + now);

                int radius = 5;
                World world = loc.getWorld();

                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            Location fireLocation = loc.clone().add(x, y, z);

                            double distance = loc.distance(fireLocation);

                            if (distance <= radius && Math.random() > 0.2) {
                                Block block = fireLocation.getBlock();

                                if (block.getType() == Material.AIR) {
                                    Block blockBelow = fireLocation.clone().add(0, -1, 0).getBlock();
                                    if (blockBelow.getType().isSolid()) {
                                        block.setType(Material.FIRE);
                                    }
                                }
                            }
                        }
                    }
                }
                molotov.getDroppedItem().remove();
                iterator.remove();
            }
        }
    }


    @EventHandler
    public void onSecond(SecondTickEvent event) {
        checkGrenades();
        checkMolotovs();
    }
}
