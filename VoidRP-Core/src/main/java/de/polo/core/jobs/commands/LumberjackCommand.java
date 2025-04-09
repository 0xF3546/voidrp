package de.polo.core.jobs.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.jobs.MiningJob;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.api.VoidAPI;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Utils;
import de.polo.core.manager.ItemManager;
import de.polo.core.location.services.impl.LocationManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.enums.EXPType;
import de.polo.core.utils.player.Progress;
import de.polo.core.utils.player.SoundManager;
import net.kyori.adventure.text.Component;
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
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static de.polo.core.Main.locationManager;
import static de.polo.core.Main.playerService;

@CommandBase.CommandMeta(
        name = "holzfäller",
        usage = "/holzfaeller"
)
public class LumberjackCommand extends CommandBase implements MiningJob {

    public LumberjackCommand(@NotNull CommandMeta meta) {
        super(meta);
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

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData asd, @NotNull String[] args) throws Exception {
        if (ServerManager.canDoJobs()) {
            if (locationManager.getDistanceBetweenCoords(player, "holzfaeller") <= 5) {
                InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §7Holzfäller"), true, true);
                if (!playerService.isInJobCooldown(player, MiniJob.LUMBERJACK) && player.getActiveJob() == null) {
                    inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aHolzfäller starten")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            startJob(player);
                            player.getPlayer().closeInventory();
                        }
                    });
                } else {
                    if (player.getActiveJob() == null) {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mHolzfäller starten", "§8 ➥§7 Warte noch " + Utils.getTime(playerService.getJobCooldown(player, MiniJob.LUMBERJACK)) + "§7.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {

                            }
                        });
                    } else {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mHolzfäller starten", "§8 ➥§7 Du hast bereits den §f" + player.getMiniJob().getName() + "§7 Job angenommen.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                            }
                        });
                    }
                }
                if (player.getActiveJob() == null) {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                } else {
                    if (!player.getMiniJob().equals(MiniJob.LUMBERJACK)) {
                        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {

                            }
                        });
                    } else {
                        if (player.getVariable("lumberjack::hasStripped") != null) {
                            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Du erhälst §a" + (int) player.getVariable("holzkg") * ServerManager.getPayout("holz") + "$")) {
                                @Override
                                public void onClick(InventoryClickEvent event) {
                                    quitJob(player);
                                    player.getPlayer().closeInventory();
                                }
                            });
                        } else {
                            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Du erhälst §cnichts§7.")) {
                                @Override
                                public void onClick(InventoryClickEvent event) {
                                    quitJob(player);
                                    player.getPlayer().closeInventory();
                                }
                            });
                        }
                    }
                }
                if (player.getActiveJob() != null && player.getMiniJob() == MiniJob.LUMBERJACK) {
                    inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.STRIPPED_OAK_WOOD, 1, 0, "§eHolz entrinden")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            if (player.getVariable("lumberjack::stripping") != null) {
                                boolean isStripping = (boolean) player.getVariable("lumberjack::stripping");
                                if (isStripping) {
                                    return;
                                }
                            }
                            player.getPlayer().closeInventory();
                            Progress.startWithTitle(player.getPlayer(), 12);
                            player.setVariable("lumberjack::stripping", true);
                            Utils.waitSeconds(12, () -> {
                                player.setVariable("lumberjack::hasStripped", true);
                                player.sendMessage("§8[§7Holzfäller§8]§7 Du hast den Baum entrindet und kannst diesen nun Verkaufen.");
                            });
                        }
                    });
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Du bist §cnicht§7 in der nähe der Holzfällerei§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
    }

    public void quitJob(VoidPlayer player) {
        Main.getInstance().beginnerpass.didQuest(player.getPlayer(), 5);
        boolean hasStripped = (boolean) player.getVariable("lumberjack::hasStripped");
        if (!hasStripped) {
            player.sendMessage("§8[§7Holzfäller§8]§7 Du hast den Job beendet.");
            return;
        }
        player.setVariable("lumberjack::hasStripped", null);
        int payout = ServerManager.getPayout("holz") * (int) player.getVariable("holzkg");
        player.sendMessage("§8[§7Holzfäller§8]§7 Vielen Dank für die geleistete Arbeit. §a+" + payout + "$");
        SoundManager.successSound(player.getPlayer());
        playerService.handleJobFinish(player, MiniJob.LUMBERJACK, 3600, Utils.random(12, 20));
        player.getPlayer().closeInventory();
        player.getData().addBankMoney(payout, "Auszahlung Holzfäller");
        Inventory inv = player.getPlayer().getInventory();
        for (ItemStack item : inv.getContents()) {
            // ISSUE VRP-10000: fixed by adding null check for item meta
            if (item != null && item.hasItemMeta() && item.getItemMeta().getDisplayName().equalsIgnoreCase("§7Holzfälleraxt")) {
                inv.removeItem(item);
            }
        }
    }

    @Override
    public void startJob(VoidPlayer player) {
        if (!playerService.isInJobCooldown(player, MiniJob.LUMBERJACK)) {
            player.setMiniJob(MiniJob.LUMBERJACK);
            player.setActiveJob(this);

            player.setVariable("lumberjack::stripping", false);
            player.setVariable("lumberjack::hasStripped", false);
            player.setVariable("job", "Holzfäller");
            player.sendMessage("§8[§7Holzfäller§8]§7 Du bist nun Holzfäller.");
            int trees = 6;
            trees += (player.getData().getJobSkill(MiniJob.LUMBERJACK).getLevel() / 2);
            player.sendMessage("§8[§7Holzfäller§8]§7 Baue §e" + trees + " Bäume§7 ab.");
            player.setVariable("holz", trees);
            player.setVariable("holzkg", 0);
            if (player.getData().getJobSkill(MiniJob.LUMBERJACK).getLevel() < 5) {
                player.getPlayer().getInventory().addItem(ItemManager.createItem(Material.WOODEN_AXE, 1, 0, "§7Holzfälleraxt"));
            } else {
                player.getPlayer().getInventory().addItem(ItemManager.createItem(Material.STONE_AXE, 1, 0, "§7Holzfälleraxt"));
            }
        } else {
            player.sendMessage("§8[§7Holzfäller§8]§7 Du kannst den Job erst in §f" + Utils.getTime(playerService.getJobCooldown(player, MiniJob.LUMBERJACK)) + "§7 beginnen.");
        }
    }

    @Override
    public void endJob(VoidPlayer player) {

    }

    @Override
    public void handleBlockBreak(VoidPlayer player, BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.OAK_LOG) {
            if ((int) player.getVariable("holz") <= 0) {
                player.sendMessage("§8[§7Holzfäller§8]§7 Du hast genug Bäume gefällt.");
                return;
            }
            player.setVariable("holz", (int) player.getVariable("holz") - 1);
            event.getBlock().setType(Material.AIR);
            int amount = Utils.random(2, 4);
            player.setVariable("holzkg", (int) player.getVariable("holzkg") + amount);
            player.sendMessage("§8[§7Holzfäller§8]§7 +" + amount + " KG Holz");
            //playerData.getScoreboard("lumberjack").updateLumberjackScoreboard();
            if ((int) player.getVariable("holz") <= 0) {
                player.sendMessage("§8[§7Holzfäller§8]§7 Du hast genug Bäume gefällt, begib dich wieder zur Holzfällerei und entrinde das Holz.");
            }
            removeTree(event.getBlock().getLocation());
            scheduleTreeRespawn(event.getBlock().getLocation());
            playerService.addExp(player.getPlayer(), EXPType.SKILL_LUMBERJACK, Utils.random(12, 20));
            /*Main.waitSeconds(120, () -> {
                block.setType(Material.OAK_LOG);
            });*/
        }
    }
}
