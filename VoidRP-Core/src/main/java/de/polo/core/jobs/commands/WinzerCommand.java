package de.polo.core.jobs.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.jobs.MiningJob;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.api.VoidAPI;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Utils;
import de.polo.core.manager.*;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.player.SoundManager;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import static de.polo.core.Main.*;

public class WinzerCommand implements CommandExecutor, MiningJob {

    public WinzerCommand() {
        Main.registerCommand("winzer", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerService.getPlayerData(player.getUniqueId());
        if (ServerManager.canDoJobs()) {
            if (locationService.getDistanceBetweenCoords(player, "winzer") <= 5) {
                InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§8 » §5Winzer"));
                if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "winzer") && playerData.getVariable("job") == null) {
                    inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aWinzer starten")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            startJob(VoidAPI.getPlayer(player));
                            player.closeInventory();
                        }
                    });
                } else {
                    if (playerData.getVariable("job") == null) {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mWinzer starten", "§8 ➥§7 Warte noch " + Utils.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "winzer")) + "§7.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {

                            }
                        });
                    } else {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mWinzer starten", "§8 ➥§7 Du hast bereits den §f" + playerData.getVariable("job") + "§7 Job angenommen.")) {
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
                    if (!playerData.getVariable("job").toString().equalsIgnoreCase("Winzer")) {
                        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {

                            }
                        });
                    } else {
                        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Du erhälst §a" + (int) playerData.getVariable("winzer_harvested") * ServerManager.getPayout("winzer") + "$")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                                player.closeInventory();
                                endJob(VoidAPI.getPlayer(player));
                            }
                        });
                    }
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Du bist §cnicht§7 in der nähe der Winzer§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
        return false;
    }

    @Override
    public void handleBlockBreak(VoidPlayer player, BlockBreakEvent event) {
        event.setCancelled(true);
        if (event.getBlock().getType() != Material.JUNGLE_LEAVES) {
            return;
        }
        if ((int) player.getVariable("winzer") <= 0) {
            return;
        }
        Block grapevine = (Block) player.getVariable("grapevine");
        if (grapevine.getLocation().getX() != event.getBlock().getLocation().getX() || grapevine.getLocation().getY() != event.getBlock().getLocation().getY() || grapevine.getLocation().getY() != event.getBlock().getY()) {
            player.sendMessage("§8[§5Winzer§8]§7 Das ist der falsche Rebstock.");
            return;
        }
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 54, Component.text("§5Rebstock"), true, true);
        for (int i = 0; i < Utils.random(10, 15); i++) {
            inventoryManager.setItem(new CustomItem(Utils.random(0, 53), ItemManager.createItem(Material.PURPLE_DYE, 1, 0, "§5Weintraube")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    ItemStack item = event.getCurrentItem();
                    item.setType(Material.BLACK_STAINED_GLASS_PANE);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("§5");
                    item.setItemMeta(meta);
                    event.setCurrentItem(item);
                    int amount = 0;
                    for (ItemStack inventoryItem : event.getInventory().getContents()) {
                        if (inventoryItem.getType() == Material.PURPLE_DYE) {
                            amount++;
                        }
                    }
                    if (amount > 0) {
                        return;
                    }
                    player.getPlayer().closeInventory();
                    checked(player);
                }
            });
        }
    }

    private void checked(VoidPlayer player) {
        Main.getInstance().seasonpass.didQuest(player.getPlayer(), 9);
        player.setVariable("winzer", (int) player.getVariable("winzer") - 1);
        player.setVariable("winzer_harvested", (int) player.getVariable("winzer_harvested") + Utils.random(1, 3));
        if ((int) player.getVariable("winzer") == 0) {
            player.sendMessage("§8[§5Winzer§8]§7 Du hast alle Weinreben abgeerntet, kehre nun zum Winzer zurück.");
            return;
        }
        findGrapevine(player.getPlayer());
    }

    private void findGrapevine(Player player) {
        int searchRadius = 100;
        Block targetBlock = null;

        int startX = player.getLocation().getBlockX() - searchRadius / 2;
        int startY = player.getLocation().getBlockY() - searchRadius / 2;
        int startZ = player.getLocation().getBlockZ() - searchRadius / 2;

        for (int i = 0; i < 100; i++) {
            int x = startX + (int) (Math.random() * searchRadius);
            int y = startY + (int) (Math.random() * searchRadius);
            int z = startZ + (int) (Math.random() * searchRadius);

            Block currentBlock = player.getWorld().getBlockAt(x, y, z);
            if (currentBlock.getType() == Material.JUNGLE_LEAVES) {
                // Überprüfe, ob unter den JUNGLE_LEAVES ein OAK_FENCE ist
                Block blockBelow = player.getWorld().getBlockAt(x, y - 1, z);
                if (blockBelow.getType() == Material.SPRUCE_FENCE) {
                    // Gefunden!
                    targetBlock = currentBlock;
                    break;
                }
            }
        }

        if (targetBlock != null) {
            PlayerData playerData = playerService.getPlayerData(player);
            player.sendMessage("§8[§5Winzer§8]§7 Hier ist ein neuer Rebstock.");
            playerData.setVariable("grapevine", targetBlock);
            navigationService.createNaviByCord(player, targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
        } else {
            findGrapevine(player);
        }
    }


    public void startJob(VoidPlayer player) {
        if (!playerService.isInJobCooldown(player, MiniJob.WINZER)) {
            player.setActiveJob(this);
            player.setMiniJob(MiniJob.WINZER);
            player.sendMessage("§8[§5Winzer§8]§7 Du bist nun Winzer.");
            int trees = 6;
            player.sendMessage("§8[§5Winzer§8]§7 Begib dich zu den markierten Weinreben.");
            player.sendMessage(Prefix.infoPrefix + "Nutze \"/rebstock\" falls du deinen Rebstock verloren hast.");
            player.setVariable("winzer", 12);
            player.setVariable("winzer_harvested", 0);
            /*Scoreboard scoreboard = new Scoreboard(player);
            scoreboard.createWinzerScoreboard();
            playerData.setScoreboard("winzer", scoreboard);*/
            player.getPlayer().getInventory().addItem(ItemManager.createItem(Material.SHEARS, 1, 0, "§5Werkzeug"));
            findGrapevine(player.getPlayer());
        } else {
            player.sendMessage("§8[§5Winzer§8]§7 Du kannst den Job erst in §f" + Utils.getTime(playerService.getJobCooldown(player, MiniJob.WINZER)) + "§7 beginnen.");
        }
    }

    @SneakyThrows
    @Override
    public void endJob(VoidPlayer player) {
        Main.getInstance().beginnerpass.didQuest(player.getPlayer(), 5);
        int payout = ServerManager.getPayout("winzer") * (int) player.getVariable("winzer_harvested");
        player.sendMessage("§8[§5Winzer§8]§7 Vielen Dank für die geleistete Arbeit. §a+" + payout + "$");
        SoundManager.successSound(player.getPlayer());
        player.getPlayer().closeInventory();
        playerService.addBankMoney(player.getPlayer(), payout, "Auszahlung Winzer");
        playerService.handleJobFinish(player, MiniJob.WINZER, 600, Utils.random(12, 20));
        Inventory inv = player.getPlayer().getInventory();
        for (ItemStack item : inv.getContents()) {
            // ISSUE VPR-10003: Added null check for item
            if (item != null && item.getType() == Material.SHEARS) {
                inv.removeItem(item);
            }
        }
    }
}
