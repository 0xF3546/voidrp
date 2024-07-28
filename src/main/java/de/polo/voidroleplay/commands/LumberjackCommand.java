package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.enums.EXPType;
import de.polo.voidroleplay.utils.playerUtils.Progress;
import de.polo.voidroleplay.utils.playerUtils.Scoreboard;
import de.polo.voidroleplay.utils.playerUtils.SoundManager;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.LocationManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.ServerManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;

public class LumberjackCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;

    public LumberjackCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.registerCommand("holzfäller", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (Main.getInstance().serverManager.canDoJobs()) {
            if (locationManager.getDistanceBetweenCoords(player, "holzfaeller") <= 5) {
                InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §7Holzfäller", true, true);
                if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "holzfäller") && playerData.getVariable("job") == null) {
                    inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aHolzfäller starten")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            Main.getInstance().commands.lumberjackCommand.startJob(player);
                            player.closeInventory();
                        }
                    });
                } else {
                    if (playerData.getVariable("job") == null) {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mHolzfäller starten", "§8 ➥§7 Warte noch " + Main.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "holzfäller")) + "§7.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {

                            }
                        });
                    } else {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mHolzfäller starten", "§8 ➥§7 Du hast bereits den §f" + playerData.getVariable("job") + "§7 Job angenommen.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                            }
                        });
                    }
                }
                if (playerData.getVariable("job") == null) {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                } else {
                    if (!playerData.getVariable("job").toString().equalsIgnoreCase("Holzfäller")) {
                        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {

                            }
                        });
                    } else {
                        if (playerData.getVariable("lumberjack::hasStripped") != null) {
                            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Du erhälst §a" + playerData.getIntVariable("holzkg") * Main.getInstance().serverManager.getPayout("holz") + "$")) {
                                @Override
                                public void onClick(InventoryClickEvent event) {
                                    quitJob(player, false);
                                    player.closeInventory();
                                }
                            });
                        } else {
                            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Du erhälst §cnichts§7.")) {
                                @Override
                                public void onClick(InventoryClickEvent event) {
                                    quitJob(player, false);
                                    player.closeInventory();
                                }
                            });
                        }
                    }
                }
                if (playerData.getVariable("job") != null && playerData.getVariable("job").toString().equalsIgnoreCase("Holzfäller")) {
                    inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.STRIPPED_OAK_WOOD, 1, 0, "§eHolz entrinden")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            if (playerData.getVariable("lumberjack::stripping") != null) {
                                boolean isStripping = playerData.getVariable("lumberjack::stripping");
                                if (isStripping) {
                                    return;
                                }
                            }
                            player.closeInventory();
                            Progress.startWithTitle(player, 12);
                            playerData.setVariable("lumberjack::stripping", true);
                            Main.waitSeconds(12, () -> {
                                playerData.setVariable("lumberjack::hasStripped", true);
                                player.sendMessage("§8[§7Holzfäller§8]§7 Du hast den Baum entrindet und kannst diesen nun Verkaufen.");
                            });
                        }
                    });
                }
            } else {
                player.sendMessage(Main.error + "Du bist §cnicht§7 in der nähe der Holzfällerei§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
        return false;
    }

    public void blockBroken(Player player, Block block, BlockBreakEvent event) {
        event.setCancelled(true);
        if (block.getType() == Material.OAK_LOG) {
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            if (playerData.getIntVariable("holz") <= 0) {
                player.sendMessage("§8[§7Holzfäller§8]§7 Du hast genug Bäume gefällt.");
                return;
            }
            playerData.setIntVariable("holz", playerData.getIntVariable("holz") - 1);
            block.setType(Material.AIR);
            int amount = Main.random(2, 4);
            playerData.setIntVariable("holzkg", playerData.getIntVariable("holzkg") + amount);
            player.sendMessage("§8[§7Holzfäller§8]§7 +" + amount + " KG Holz");
            playerData.getScoreboard("lumberjack").updateLumberjackScoreboard();
            if (playerData.getIntVariable("holz") <= 0) {
                player.sendMessage("§8[§7Holzfäller§8]§7 Du hast genug Bäume gefällt, begib dich wieder zur Holzfällerei und entrinde das Holz.");
            }
            removeTree(block.getLocation());
            scheduleTreeRespawn(block.getLocation());
            playerManager.addExp(player, EXPType.SKILL_LUMBERJACK, Main.random(12, 20));
            /*Main.waitSeconds(120, () -> {
                block.setType(Material.OAK_LOG);
            });*/
        }
    }

    private static void removeTree(Location startLocation) {
        Block startBlock = startLocation.getBlock();
        removeWoodBlocks(startBlock);
    }

    private static void removeWoodBlocks(Block block) {
        if (block.getType() == Material.OAK_LOG) {
            block.setType(Material.AIR);
            for (int xOffset = -1; xOffset <= 1; xOffset++) {
                for (int yOffset = 0; yOffset <= 1; yOffset++) {
                    for (int zOffset = -1; zOffset <= 1; zOffset++) {
                        Block relativeBlock = block.getRelative(xOffset, yOffset, zOffset);
                        removeWoodBlocks(relativeBlock);
                    }
                }
            }
        }
    }

    private static void scheduleTreeRespawn(Location location) {
        new BukkitRunnable() {
            @Override
            public void run() {
                respawnTree(location);
            }
        }.runTaskLater(Main.getInstance(), 120 * 20);
    }

    private static void respawnTree(Location location) {
        Block block = location.getBlock();
        if (block.getType() == Material.AIR) {
            block.setType(Material.OAK_LOG);
        }
    }

    public void quitJob(Player player, boolean silent) {
        Main.getInstance().beginnerpass.didQuest(player, 5);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("job", null);
        boolean hasStripped = playerData.getVariable("lumberjack::hasStripped");
        if (!hasStripped) {
            player.sendMessage("§8[§7Holzfäller§8]§7 Du hast den Job beendet.");
            return;
        }
        playerData.setVariable("lumberjack::hasStripped", null);
        int payout = Main.getInstance().serverManager.getPayout("holz") * playerData.getIntVariable("holzkg");
        player.sendMessage("§8[§7Holzfäller§8]§7 Vielen Dank für die geleistete Arbeit. §a+" + payout + "$");
        SoundManager.successSound(player);
        if (playerData.getIntVariable("holz") <= 0) playerManager.addExp(player, Main.random(12, 20));
        playerData.getScoreboard("lumberjack").killScoreboard();
        player.closeInventory();
        try {
            playerManager.addBankMoney(player, payout, "Auszahlung Holzfäller");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Main.getInstance().getCooldownManager().setCooldown(player, "holzfäller", 600);
        Inventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§7Holzfälleraxt")) {
                inv.removeItem(item);
            }
        }
    }

    public void startJob(Player player) {
        if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "holzfäller")) {
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            playerData.setVariable("lumberjack::stripping", false);
            playerData.setVariable("lumberjack::hasStripped", false);
            playerData.setVariable("job", "Holzfäller");
            player.sendMessage("§8[§7Holzfäller§8]§7 Du bist nun Holzfäller.");
            int trees = 6;
            trees += (playerData.addonXP.getLumberjackLevel() / 2);
            player.sendMessage("§8[§7Holzfäller§8]§7 Baue §e" + trees + " Bäume§7 ab.");
            playerData.setIntVariable("holz", trees);
            playerData.setIntVariable("holzkg", 0);
            Scoreboard scoreboard = new Scoreboard(player);
            scoreboard.createLumberjackScoreboard();
            playerData.setScoreboard("lumberjack", scoreboard);
            if (playerData.addonXP.getLumberjackLevel() < 5) {
                player.getInventory().addItem(ItemManager.createItem(Material.WOODEN_AXE, 1, 0, "§7Holzfälleraxt"));
            } else {
                player.getInventory().addItem(ItemManager.createItem(Material.STONE_AXE, 1, 0, "§7Holzfälleraxt"));
            }
        } else {
            player.sendMessage("§8[§7Holzfäller§8]§7 Du kannst den Job erst in §f" + Main.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "holzfäller")) + "§7 beginnen.");
        }
    }
}
