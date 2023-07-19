package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.PlayerUtils.SoundManager;
import de.polo.metropiacity.Utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;

public class FarmerCommand implements CommandExecutor {
    public static String prefix = "§8[§eFarmer§8] §7";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        if (ServerManager.canDoJobs()) {
            if (LocationManager.getDistanceBetweenCoords(player, "farmer") <= 5) {
                playerData.setVariable("current_inventory", "farmer");
                Inventory inv = Bukkit.createInventory(player, 27, "§8 » §eFarmer");
                if (!Main.cooldownManager.isOnCooldown(player, "farmer") && playerData.getVariable("job") == null) {
                    inv.setItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aFarmer starten", null));
                    inv.setItem(22, ItemManager.createItem(Material.WHEAT, 1, 0, "§eWeizenlieferant starten", null));
                } else {
                    if (playerData.getVariable("job") == null) {
                        inv.setItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mFarmer starten", "§8 ➥§7 Warte noch " + Main.getTime(Main.cooldownManager.getRemainingTime(player, "farmer")) + "§7."));
                        inv.setItem(22, ItemManager.createItem(Material.WHEAT, 1, 0, "§eWeizenlieferant starten", null));
                    } else {
                        inv.setItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mJob starten", "§8 ➥§7 Du hast bereits den §f" + playerData.getVariable("job") + "§7 Job angenommen."));
                        inv.setItem(22, ItemManager.createItem(Material.WHEAT, 1, 0, "§e§mWeizenlieferant starten", "§8 ➥§7 Du hast bereits den §f" + playerData.getVariable("job") + "§7 Job angenommen."));
                    }
                }
                if (playerData.getVariable("job") != "farmer" && playerData.getVariable("job") != "weizenlieferant") {
                    inv.setItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen"));
                } else {
                    if (playerData.getVariable("job") == "farmer") {
                        inv.setItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Du erhälst §a" + ServerManager.getPayout("heuballen") * playerData.getIntVariable("heuballen") + "$"));
                    } else {
                        inv.setItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Weizenlieferant beenden"));
                    }
                }
                for (int i = 0; i < 27; i++) {
                    if (inv.getItem(i) == null) {
                        inv.setItem(i, ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, "§8", null));
                    }
                }
                player.openInventory(inv);
            } else {
                player.sendMessage(Main.error + "Du bist §cnicht§7 in der nähe der Farm§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
        return false;
    }

    public static void quitJob(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getVariable("job") == "weizenlieferant") {
            playerData.setVariable("job", null);
            player.sendMessage("§8[§eLieferant§8]§7 Du hast den Job beendet.");
            playerData.getScoreboard().killScoreboard();
            return;
        }
        playerData.setVariable("job", null);
        int payout = ServerManager.getPayout("heuballen") * playerData.getIntVariable("heuballen");
        player.sendMessage("§8[§eFarmer§8]§7 Vielen Dank für die geleistete Arbeit. §a+" + payout + "$");
        SoundManager.successSound(player);
        if (playerData.getIntVariable("heuballen_remaining") <= 0) PlayerManager.addExp(player, Main.random(12, 20));
        playerData.getScoreboard().killScoreboard();
        player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
        try {
            PlayerManager.addBankMoney(player, payout, "Auszahlung Farmer");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Main.cooldownManager.setCooldown(player, "farmer", 600);
    }

    public static void blockBroken(Player player, Block block, BlockBreakEvent event) {
        event.setCancelled(true);
        if (block.getType() == Material.HAY_BLOCK) {
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            if (playerData.getIntVariable("heuballen_remaining") <= 0) {
                player.sendMessage("§8[§eFarmer§8]§7 Du hast alle heuballen abgebaut, begib dich wieder zum Farmer.");
                return;
            }
            block.setType(Material.AIR);
            playerData.setIntVariable("heuballen_remaining", playerData.getIntVariable("heuballen_remaining") - 1);
            int amount = Main.random(2, 4);
            playerData.setIntVariable("heuballen", playerData.getIntVariable("heuballen") + amount);
            player.sendMessage("§8[§eFarmer§8]§7 +" + amount + " Heuballen");
            playerData.getScoreboard().updateFarmerScoreboard();
            if (playerData.getIntVariable("heuballen_remaining") <= 0) {
                player.sendMessage("§8[§eFarmer§8]§7 Du hast alle heuballen abgebaut, begib dich wieder zum Farmer.");
            }
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                @Override
                public void run() {
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.HAY_BLOCK);
                    }
                }
            }, 2 * 60 * 20);
        }
    }

    public static void startJob(Player player) {
        if (!Main.cooldownManager.isOnCooldown(player, "farmer")) {
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            playerData.setVariable("job", "farmer");
            player.sendMessage(prefix + "Du bist nun §eFarmer§7.");
            player.sendMessage(prefix + "Baue §e" + player.getName() + " Heuballen§7 ab.");
            playerData.setIntVariable("heuballen_remaining", 9);
            playerData.setIntVariable("heuballen", 0);
            playerData.getScoreboard().createFarmerScoreboard();
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 0, true, false));
        } else {
            player.sendMessage("§8[§eFarmer§8]§7 Du kannst den Job erst in §f" + Main.getTime(Main.cooldownManager.getRemainingTime(player, "farmer")) + "§7 beginnen.");
        }
    }

    public static void startTransport(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setIntVariable("weizen", Main.random(2, 5));
        playerData.setVariable("job", "weizenlieferant");
        playerData.getScoreboard().createWeizentransportScoreboard();
        player.sendMessage("§8[§eLieferant§8]§7 Bringe das Weizen zur Mühle.");
        player.sendMessage("§8 ➥ §7Nutze §8/§edrop§7 um das Weizen abzugeben.");
        Navigation.createNavi(player, "Mühle", true);
    }

    public static void dropTransport(Player player) {
        if (LocationManager.getDistanceBetweenCoords(player, "Mühle") < 5) {
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            int payout = Main.random(ServerManager.getPayout("weizenlieferant"), ServerManager.getPayout("weizenlieferant2"));
            player.sendMessage("§8[§eLieferant§8]§7 Danke für's abliefern. §a+" + payout + "$");
            SoundManager.successSound(player);
            PlayerManager.addExp(player, Main.random(1, 3));
            playerData.setIntVariable("weizen", playerData.getIntVariable("weizen") - 1);
            playerData.getScoreboard().updateWeizentransportScoreboard();
            try {
                PlayerManager.addBankMoney(player, payout, "Auszahlung Weizentransport");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (playerData.getIntVariable("weizen") <= 0) {
                player.sendMessage("§8[§eLieferant§8]§7 Du hast alles abgegeben. Danke!");
                playerData.setVariable("job", null);
                playerData.getScoreboard().killScoreboard();
            }
        } else {
            player.sendMessage(Main.error + "Du bist nicht in der nähe der Mühle.");
        }
    }
}
