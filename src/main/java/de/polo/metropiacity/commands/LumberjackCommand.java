package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.playerUtils.SoundManager;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.LocationManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;

public class LumberjackCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        if (ServerManager.canDoJobs()) {
            if (LocationManager.getDistanceBetweenCoords(player, "holzfaeller") <= 5) {
                playerData.setVariable("current_inventory", "holzfäller");
                Inventory inv = Bukkit.createInventory(player, 27, "§8 » §7Holzfäller");
                if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "holzfäller") && playerData.getVariable("job") == null) {
                    inv.setItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aHolzfäller starten", null));
                } else {
                    if (playerData.getVariable("job") == null) {
                        inv.setItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mHolzfäller starten", "§8 ➥§7 Warte noch " + Main.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "holzfäller")) + "§7."));
                    } else {
                        inv.setItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mHolzfäller starten", "§8 ➥§7 Du hast bereits den §f" + playerData.getVariable("job") + "§7 Job angenommen."));
                    }
                }
                if (playerData.getVariable("job") == null) {
                    inv.setItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen"));
                } else {
                    if (!playerData.getVariable("job").equals("Holzfäller")) {
                        inv.setItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen"));
                    } else {
                        inv.setItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Du erhälst §a" + playerData.getIntVariable("holzkg") * ServerManager.getPayout("holz") + "$"));
                    }
                }
                for (int i = 0; i < 27; i++) {
                    if (inv.getItem(i) == null) {
                        inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                    }
                }
                player.openInventory(inv);
            } else {
                player.sendMessage(Main.error + "Du bist §cnicht§7 in der nähe der Holzfällerei§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
        return false;
    }

    public static void blockBroken(Player player, Block block, BlockBreakEvent event) {
        event.setCancelled(true);
        if (block.getType() == Material.OAK_LOG) {
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            if (playerData.getIntVariable("holz") <= 0) {
                player.sendMessage("§8[§7Holzfäller§8]§7 Du hast genug Bäume gefällt.");
                return;
            }
            playerData.setIntVariable("holz", playerData.getIntVariable("holz") - 1);
            block.setType(Material.AIR);
            int amount = Main.random(2, 4);
            playerData.setIntVariable("holzkg", playerData.getIntVariable("holzkg") + amount);
            player.sendMessage("§8[§7Holzfäller§8]§7 +" + amount + " KG Holz");
            playerData.getScoreboard().updateLumberjackScoreboard();
            if (playerData.getIntVariable("holz") <= 0) {
                player.sendMessage("§8[§7Holzfäller§8]§7 Du hast genug Bäume gefällt, begib dich wieder zur Holzfällerei.");
            }
            removeTree(block.getLocation());
            scheduleTreeRespawn(block.getLocation());
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

    public static void quitJob(Player player, boolean silent) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setVariable("job", null);
        int payout = ServerManager.getPayout("holz") * playerData.getIntVariable("holzkg");
        player.sendMessage("§8[§7Holzfäller§8]§7 Vielen Dank für die geleistete Arbeit. §a+" + payout + "$");
        SoundManager.successSound(player);
        if (playerData.getIntVariable("holz") <= 0) PlayerManager.addExp(player, Main.random(12, 20));
        playerData.getScoreboard().killScoreboard();
        player.closeInventory();
        try {
            PlayerManager.addBankMoney(player, payout, "Auszahlung Holzfäller");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Main.getInstance().getCooldownManager().setCooldown(player, "holzfäller", 600);
        Inventory inv = player.getInventory();
            for (ItemStack item : inv.getContents()) {
                if (item.getType() == Material.WOODEN_AXE) {
                    inv.removeItem(item);
                }
            }
    }
    public static void startJob(Player player) {
        if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "holzfäller")) {
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            playerData.setVariable("job", "Holzfäller");
            player.sendMessage("§8[§7Holzfäller§8]§7 Du bist nun Holzfäller.");
            player.sendMessage("§8[§7Holzfäller§8]§7 Baue §e6 Bäume§7 ab.");
            playerData.setIntVariable("holz", 6);
            playerData.setIntVariable("holzkg", 0);
            playerData.getScoreboard().createLumberjackScoreboard();
            player.getInventory().addItem(ItemManager.createItem(Material.WOODEN_AXE, 1, 0, "§7Holzaxt", null));
        } else {
            player.sendMessage("§8[§7Holzfäller§8]§7 Du kannst den Job erst in §f" + Main.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "holzfäller")) + "§7 beginnen.");
        }
    }
}
