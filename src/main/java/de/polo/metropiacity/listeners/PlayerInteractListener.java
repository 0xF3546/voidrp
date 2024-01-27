package de.polo.metropiacity.listeners;

import com.jeff_media.customblockdata.CustomBlockData;
import de.polo.metropiacity.dataStorage.*;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.*;
import de.polo.metropiacity.utils.Game.Laboratory;
import de.polo.metropiacity.utils.GamePlay.GamePlay;
import de.polo.metropiacity.utils.InventoryManager.CustomItem;
import de.polo.metropiacity.utils.InventoryManager.InventoryManager;
import de.polo.metropiacity.utils.enums.Drug;
import de.polo.metropiacity.utils.enums.RoleplayItem;
import de.polo.metropiacity.utils.playerUtils.ChatUtils;
import de.polo.metropiacity.utils.playerUtils.Rubbellose;
import de.polo.metropiacity.utils.Game.Housing;
import jdk.vm.ci.code.Register;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class PlayerInteractListener implements Listener {
    private final PlayerManager playerManager;
    private final Utils utils;
    private final Laboratory laboratory;
    private final FactionManager factionManager;
    private final Main.Commands commands;
    private final BlockManager blockManager;

    public PlayerInteractListener(PlayerManager playerManager, Utils utils, Main.Commands commands, BlockManager blockManager, FactionManager factionManager, Laboratory laboratory) {
        this.playerManager = playerManager;
        this.utils = utils;
        this.commands = commands;
        this.blockManager = blockManager;
        this.factionManager = factionManager;
        this.laboratory = laboratory;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setIntVariable("afk", 0);
        if (playerData.isAFK()) {
            utils.setAFK(player, false);
        }
        if (playerData.isDead()) {
            event.setCancelled(true);
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null) {
                if (event.getClickedBlock().getType() == Material.OAK_DOOR) {
                    RegisteredBlock rBlock = blockManager.getBlockAtLocation(event.getClickedBlock().getLocation());
                    if (rBlock != null) {
                        if (rBlock.getInfo().equalsIgnoreCase("fdoor")) {
                            if (!playerData.isAduty() && !playerData.getFaction().equalsIgnoreCase(rBlock.getInfoValue())) {
                                event.setCancelled(true);
                            }
                        } else if (rBlock.getInfo().equalsIgnoreCase("laboratory")) {
                            int id = Integer.parseInt(rBlock.getInfoValue());
                            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
                            if (!(factionData.getLaboratory() == id) && !laboratory.isDoorOpened(factionData)) {
                                event.setCancelled(true);
                                for (FactionData defender : factionManager.getFactions()) {
                                    if (defender.getLaboratory() == id) {
                                        if (!laboratory.isDoorOpened(factionData)) {
                                        laboratory.openLaboratoryAsAttacker(player, defender);
                                        return;
                                        }
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
                                    if (!playerData.isAduty() && !utils.housing.canPlayerInteract(player, Integer.parseInt(registeredBlock.getInfoValue()))) {
                                        event.setCancelled(true);
                                    }
                                }
                            }
                        }
                    }
                }
                if (event.getClickedBlock().getType() == Material.CHEST) {
                    event.setCancelled(true);
                    RegisteredBlock registeredBlock = blockManager.getBlockAtLocation(event.getClickedBlock().getLocation());
                    if (registeredBlock.getInfo().equalsIgnoreCase("dlager")) {
                        if (registeredBlock.getInfoValue().equalsIgnoreCase(playerData.getFaction())) {
                            Main.getInstance().gamePlay.drugstorage.open(player);
                        }
                    }
                }
                RegisteredBlock factionBlock = blockManager.getBlockAtLocation(event.getClickedBlock().getLocation());
                if (factionBlock != null) {
                    if (factionBlock.getInfo().equalsIgnoreCase("factionupgrade")) {
                        if (factionBlock.getInfoValue().equalsIgnoreCase(playerData.getFaction())) {
                            Main.getInstance().gamePlay.factionUpgradeGUI.open(player);
                        }
                    }
                }
                if (event.getClickedBlock().getType() == Material.CAULDRON) {
                    Material[] items = {Material.POTATO, Material.POISONOUS_POTATO, Material.GLASS_BOTTLE};
                    if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "mülleimer")) {
                        Main.getInstance().getCooldownManager().setCooldown(player, "mülleimer", 30);
                        Material random = items[new Random().nextInt(items.length)];
                        player.getInventory().addItem(new ItemStack(random));
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
                            player.sendMessage(Main.error + "Dieser Automat wurde noch nicht registriert.");
                        }
                        PersistentDataContainer container = new CustomBlockData(event.getClickedBlock(), Main.plugin);
                        RegisteredBlock block = blockManager.getBlockAtLocation(event.getClickedBlock().getLocation());
                        if (Objects.equals(block.getInfo(), "house")) {
                            HouseData houseData = utils.housing.getHouse(Integer.parseInt(block.getInfoValue()));
                            playerData.setIntVariable("current_house", houseData.getNumber());
                            InventoryManager inventoryManager = new InventoryManager(player, 45, "", true, true);
                            if (houseData.getOwner() != null) {
                                OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(houseData.getOwner()));
                                inventoryManager.setItem(new CustomItem(13, ItemManager.createItemHead(houseData.getOwner(), 1, 0, "§6Besitzer", "§8 ➥ §7" + owner.getName())) {
                                    @Override
                                    public void onClick(InventoryClickEvent event) {

                                    }
                                });
                                if (houseData.getOwner().equals(player.getUniqueId().toString())) {
                                    inventoryManager.setItem(new CustomItem(33, ItemManager.createItem(Material.RED_DYE, 1, 0, "§cHaus verkaufen", "§8 ➥§7 Du erhälst: " + new DecimalFormat("#,###").format(houseData.getPrice() * 0.8) + "$")) {
                                        @Override
                                        public void onClick(InventoryClickEvent event) {
                                            if (utils.housing.resetHouse(player, playerData.getIntVariable("current_house"))) {
                                                HouseData houseData = Housing.houseDataMap.get(playerData.getIntVariable("current_house"));
                                                playerData.addMoney((int) (houseData.getPrice() * 0.8));
                                                player.sendMessage("§8[§6Haus§8]§a Du hast Haus " + houseData.getNumber() + " für " + (int) (houseData.getPrice() * 0.8) + "$ verkauft.");
                                                player.closeInventory();
                                            }
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
                                        player.performCommand("buyhouse " + playerData.getIntVariable("current_house"));
                                        player.closeInventory();
                                    }
                                });
                            }
                            inventoryManager.setItem(new CustomItem(29, ItemManager.createItem(Material.PAPER, 1, 0, "§bInformationen", Arrays.asList("§8 ➥ §ePreis§8:§7 " + new DecimalFormat("#,###").format(houseData.getPrice()) + "$", "§8 ➥ §eUmsatz§8: §7" + new DecimalFormat("#,###").format(houseData.getTotalMoney()) + "$", "§8 ➥ §eMieterslots§8:§7 " + houseData.getTotalSlots()))) {
                                @Override
                                public void onClick(InventoryClickEvent event) {

                                }
                            });
                            if (playerData.getVariable("job") == null) {
                                inventoryManager.setItem(new CustomItem(31, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§7Kein Job", "§8 ➥§7 Du hast keinen passenden Job angenommen")) {
                                    @Override
                                    public void onClick(InventoryClickEvent event) {

                                    }
                                });
                            } else {
                                if (!playerData.getVariable("job").toString().equalsIgnoreCase("postbote") && !playerData.getVariable("job").toString().equalsIgnoreCase("müllmann")) {
                                    inventoryManager.setItem(new CustomItem(31, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§7Kein Job", "§8 ➥§7 Du hast keinen passenden Job angenommen")) {
                                        @Override
                                        public void onClick(InventoryClickEvent event) {

                                        }
                                    });
                                } else if (playerData.getVariable("job").toString().equalsIgnoreCase("postbote")) {
                                    if (commands.postboteCommand.canGive(houseData.getNumber())) {
                                        inventoryManager.setItem(new CustomItem(31, ItemManager.createItem(Material.BOOK, 1, 0, "§ePost abgeben")) {
                                            @Override
                                            public void onClick(InventoryClickEvent event) {
                                                Main.getInstance().commands.postboteCommand.dropTransport(player, playerData.getIntVariable("current_house"));
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
                                } else if (playerData.getVariable("job").toString().equalsIgnoreCase("müllmann")) {
                                    if (commands.muellmannCommand.canGet(houseData.getNumber())) {
                                        inventoryManager.setItem(new CustomItem(31, ItemManager.createItem(Material.CAULDRON, 1, 0, "§bMüll einsammeln")) {
                                            @Override
                                            public void onClick(InventoryClickEvent event) {
                                                Main.getInstance().commands.muellmannCommand.dropTransport(player, playerData.getIntVariable("current_house"));
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


            //hier die items und nicht die blöcke


            if (event.getItem() == null) return;
            if (event.getItem().getItemMeta().getDisplayName().contains("Rubbellos")) {
                Rubbellose rubbellose = new Rubbellose(Main.getInstance().playerManager);
                rubbellose.startGame(player);
                ItemStack itemStack = event.getItem().clone();
                itemStack.setAmount(1);
                player.getInventory().removeItem(itemStack);
            } else if (event.getItem().getItemMeta().getDisplayName().contains("XP-Case")) {
                ItemStack itemStack = event.getItem().clone();
                itemStack.setAmount(1);
                player.getInventory().removeItem(itemStack);
                playerManager.addExp(player, Main.random(50, 200));
            } else if (event.getItem().getItemMeta().getDisplayName().equals("§6§lCase")) {
                //todo
            } else if (event.getItem().getItemMeta().getDisplayName().equals(RoleplayItem.JOINT.getDisplayName())) {
                InventoryManager inventoryManager = new InventoryManager(player, 27, "", true, true);
                inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(RoleplayItem.BOX_WITH_JOINTS.getMaterial(), 1, 0, RoleplayItem.BOX_WITH_JOINTS.getDisplayName(), "§8 ➥ §aVerpacke 3 Joints in einer Kiste.")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (ItemManager.getCustomItemCount(player, RoleplayItem.JOINT) < 3) {
                            player.sendMessage(Main.error + "Du hast nicht genug Joints.");
                            return;
                        }
                        ItemManager.removeCustomItem(player, RoleplayItem.JOINT, 3);
                        ItemManager.addCustomItem(player, RoleplayItem.BOX_WITH_JOINTS, 1);
                        player.sendMessage("§7Du hast eine Kiste hergestellt.");
                        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " stellt eine Kiste mit Joints her");
                        player.closeInventory();
                    }
                });
            } else if (event.getItem().getItemMeta().getDisplayName().equals(Drug.COCAINE.getItem().getDisplayName())) {
                GamePlay.useDrug(player, Drug.COCAINE);
            } else if (event.getItem().getItemMeta().getDisplayName().equals(Drug.JOINT.getItem().getDisplayName())) {
                GamePlay.useDrug(player, Drug.JOINT);
            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Drug.ANTIBIOTIKUM.getItem().getDisplayName())) {
                GamePlay.useDrug(player, Drug.ANTIBIOTIKUM);
            }
        }
    }
}
