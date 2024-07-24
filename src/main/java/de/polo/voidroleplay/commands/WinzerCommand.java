package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.playerUtils.Scoreboard;
import de.polo.voidroleplay.utils.playerUtils.SoundManager;
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

import java.sql.SQLException;

public class WinzerCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    private final Navigation navigation;

    public WinzerCommand(PlayerManager playerManager, LocationManager locationManager, Navigation navigation) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.navigation = navigation;
        Main.registerCommand("winzer", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (Main.getInstance().serverManager.canDoJobs()) {
            if (locationManager.getDistanceBetweenCoords(player, "winzer") <= 5) {
                InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §5Winzer", true, true);
                if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "winzer") && playerData.getVariable("job") == null) {
                    inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aWinzer starten")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            startJob(player);
                            player.closeInventory();
                        }
                    });
                } else {
                    if (playerData.getVariable("job") == null) {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mWinzer starten", "§8 ➥§7 Warte noch " + Main.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "winzer")) + "§7.")) {
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
                        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Du erhälst §a" + playerData.getIntVariable("winzer_harvested") * Main.getInstance().serverManager.getPayout("winzer") + "$")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                                quitJob(player, false);
                                player.closeInventory();
                            }
                        });
                    }
                }
            } else {
                player.sendMessage(Main.error + "Du bist §cnicht§7 in der nähe der Winzer§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
        return false;
    }

    public void blockBroken(Player player, Block block, BlockBreakEvent event) {
        event.setCancelled(true);
        if (event.getBlock().getType() != Material.JUNGLE_LEAVES) {
            return;
        }
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getIntVariable("winzer") <= 0) {
            return;
        }
        Block grapevine = playerData.getVariable("grapevine");
        if (grapevine.getLocation().getX() != event.getBlock().getLocation().getX() || grapevine.getLocation().getY() != event.getBlock().getLocation().getY() || grapevine.getLocation().getY() != event.getBlock().getY()) {
            player.sendMessage("§8[§5Winzer§8]§7 Das ist der falsche Rebstock.");
            return;
        }
        InventoryManager inventoryManager = new InventoryManager(player, 54, "§5Rebstock", true, true);
        for (int i = 0; i < Main.random(10, 15); i++) {
            inventoryManager.setItem(new CustomItem(Main.random(0, 53), ItemManager.createItem(Material.PURPLE_DYE, 1, 0, "§5Weintraube")) {
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
                    player.closeInventory();
                    checked(player);
                }
            });
        }
    }

    private void checked(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        playerData.setIntVariable("winzer", playerData.getIntVariable("winzer") - 1);
        playerData.setIntVariable("winzer_harvested", playerData.getIntVariable("winzer_harvested") + Main.random(1, 3));
        if (playerData.getIntVariable("winzer") == 0) {
            player.sendMessage("§8[§5Winzer§8]§7 Du hast alle Weinreben abgeerntet, kehre nun zum Winzer zurück.");
            return;
        }
        findGrapevine(player);
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
                if (blockBelow.getType() == Material.OAK_FENCE) {
                    // Gefunden!
                    targetBlock = currentBlock;
                    break;
                }
            }
        }

        if (targetBlock != null) {
            PlayerData playerData = playerManager.getPlayerData(player);
            player.sendMessage("§8[§5Winzer§8]§7 Hier ist ein neuer Rebstock.");
            playerData.setVariable("grapevine", targetBlock);
            navigation.createNaviByCord(player, targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
        } else {
            findGrapevine(player);
        }
    }


    private void startJob(Player player) {
        if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "winzer")) {
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            playerData.setVariable("job", "Winzer");
            player.sendMessage("§8[§5Winzer§8]§7 Du bist nun Winzer.");
            int trees = 6;
            trees += (playerData.addonXP.getLumberjackLevel() / 2);
            player.sendMessage("§8[§5Winzer§8]§7 Begib dich zu den markierten Weinreben.");
            player.sendMessage(Main.infoPrefix + "Nutze \"/rebstock\" falls du deinen Rebstock verloren hast.");
            playerData.setIntVariable("winzer", 12);
            playerData.setIntVariable("winzer_harvested", 0);
            Scoreboard scoreboard = new Scoreboard(player);
            scoreboard.createWinzerScoreboard();
            playerData.setScoreboard("winzer", scoreboard);
            player.getInventory().addItem(ItemManager.createItem(Material.SHEARS, 1, 0, "§5Werkzeug"));
            findGrapevine(player);
        } else {
            player.sendMessage("§8[§5Winzer§8]§7 Du kannst den Job erst in §f" + Main.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "winzer")) + "§7 beginnen.");
        }
    }

    public void quitJob(Player player, boolean silent) {
        Main.getInstance().beginnerpass.didQuest(player, 5);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setVariable("job", null);
        int payout = Main.getInstance().serverManager.getPayout("winzer") * playerData.getIntVariable("winzer_harvested");
        player.sendMessage("§8[§5Winzer§8]§7 Vielen Dank für die geleistete Arbeit. §a+" + payout + "$");
        SoundManager.successSound(player);
        if (playerData.getIntVariable("winzer") <= 0) playerManager.addExp(player, Main.random(12, 20));
        playerData.getScoreboard("winzer").killScoreboard();
        player.closeInventory();
        try {
            playerManager.addBankMoney(player, payout, "Auszahlung Winzer");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Main.getInstance().getCooldownManager().setCooldown(player, "winzer", 600);
        Inventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item.getType() == Material.SHEARS) {
                inv.removeItem(item);
            }
        }
    }
}
