package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.playerUtils.Scoreboard;
import de.polo.voidroleplay.utils.playerUtils.SoundManager;
import de.polo.voidroleplay.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;

public class FarmerCommand implements CommandExecutor {
    public final String prefix = "§8[§eFarmer§8] §7";
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    private final Utils utils;
    public FarmerCommand(PlayerManager playerManager, LocationManager locationManager, Utils utils) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.utils = utils;
        Main.registerCommand("farmer", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (ServerManager.canDoJobs()) {
            if (locationManager.getDistanceBetweenCoords(player, "farmer") <= 5) {
                InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §eFarmer", true, true);
                if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "farmer") && playerData.getVariable("job") == null) {
                    inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aFarmer starten")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            startJob(player);
                            player.closeInventory();
                        }
                    });
                    inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.WHEAT, 1, 0, "§eWeizenlieferant starten")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            startTransport(player);
                            player.closeInventory();
                        }
                    });
                } else {
                    if (playerData.getVariable("job") == null) {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mFarmer starten", "§8 ➥§7 Warte noch " + Main.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "farmer")) + "§7.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {

                            }
                        });
                        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.WHEAT, 1, 0, "§eWeizenlieferant starten")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                                startTransport(player);
                                player.closeInventory();
                            }
                        });
                    } else {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mJob starten", "§8 ➥§7 Du hast bereits den §f" + playerData.getVariable("job") + "§7 Job angenommen.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {

                            }
                        });
                        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.WHEAT, 1, 0, "§e§mWeizenlieferant starten", "§8 ➥§7 Du hast bereits den §f" + playerData.getVariable("job") + "§7 Job angenommen.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {

                            }
                        });
                    }
                }
                if (playerData.getVariable("job") != "farmer" && playerData.getVariable("job") != "weizenlieferant") {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                } else {
                    if (playerData.getVariable("job") == "farmer") {
                        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Du erhälst §a" + ServerManager.getPayout("heuballen") * playerData.getIntVariable("heuballen") + "$")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                                quitJob(player);
                                player.closeInventory();
                            }
                        });
                    } else {
                        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Weizenlieferant beenden")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {

                            }
                        });
                    }
                }
            } else {
                player.sendMessage(Main.error + "Du bist §cnicht§7 in der nähe der Farm§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
        return false;
    }

    public void quitJob(Player player) {
        Main.getInstance().beginnerpass.didQuest(player, 5);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getVariable("job") == "weizenlieferant") {
            playerData.setVariable("job", null);
            player.sendMessage("§8[§eLieferant§8]§7 Du hast den Job beendet.");
            playerData.getScoreboard("farmer").killScoreboard();
            return;
        }
        playerData.setVariable("job", null);
        int payout = ServerManager.getPayout("heuballen") * playerData.getIntVariable("heuballen");
        player.sendMessage("§8[§eFarmer§8]§7 Vielen Dank für die geleistete Arbeit. §a+" + payout + "$");
        SoundManager.successSound(player);
        if (playerData.getIntVariable("heuballen_remaining") <= 0) playerManager.addExp(player, Main.random(12, 20));
        playerData.getScoreboard("farmer").killScoreboard();
        player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
        try {
            playerManager.addBankMoney(player, payout, "Auszahlung Farmer");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Main.getInstance().getCooldownManager().setCooldown(player, "farmer", 600);
        player.closeInventory();
    }

    public void blockBroken(Player player, Block block, BlockBreakEvent event) {
        event.setCancelled(true);
        if (block.getType() == Material.HAY_BLOCK) {
            Main.getInstance().seasonpass.didQuest(player, 4);
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            if (playerData.getIntVariable("heuballen_remaining") <= 0) {
                player.sendMessage("§8[§eFarmer§8]§7 Du hast alle heuballen abgebaut, begib dich wieder zum Farmer.");
                return;
            }
            block.setType(Material.AIR);
            playerData.setIntVariable("heuballen_remaining", playerData.getIntVariable("heuballen_remaining") - 1);
            int amount = Main.random(2, 4);
            playerData.setIntVariable("heuballen", playerData.getIntVariable("heuballen") + amount);
            player.sendMessage("§8[§eFarmer§8]§7 +" + amount + " Heuballen");
            playerData.getScoreboard("farmer").updateFarmerScoreboard();
            if (playerData.getIntVariable("heuballen_remaining") <= 0) {
                player.sendMessage("§8[§eFarmer§8]§7 Du hast alle heuballen abgebaut, begib dich wieder zum Farmer.");
            }
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if (block.getType() == Material.AIR) {
                    block.setType(Material.HAY_BLOCK);
                }
            }, 2 * 60 * 20);
        }
    }

    public void startJob(Player player) {
        if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "farmer")) {
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            playerData.setVariable("job", "farmer");
            player.sendMessage(prefix + "Du bist nun §eFarmer§7.");
            player.sendMessage(prefix + "Baue §e9 Heuballen§7 ab.");
            playerData.setIntVariable("heuballen_remaining", 9);
            playerData.setIntVariable("heuballen", 0);
            Scoreboard scoreboard = new Scoreboard(player);
            scoreboard.createFarmerScoreboard();
            playerData.setScoreboard("farmer", scoreboard);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 0, true, false));
        } else {
            player.sendMessage("§8[§eFarmer§8]§7 Du kannst den Job erst in §f" + Main.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "farmer")) + "§7 beginnen.");
        }
    }

    public void startTransport(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setIntVariable("weizen", Main.random(2, 5));
        playerData.setVariable("job", "weizenlieferant");
        Scoreboard scoreboard = new Scoreboard(player);
        scoreboard.createWeizentransportScoreboard();
        playerData.setScoreboard("weizen", scoreboard);
        player.sendMessage("§8[§eLieferant§8]§7 Bringe das Weizen zur Mühle.");
        player.sendMessage("§8 ➥ §7Nutze §8/§edrop§7 um das Weizen abzugeben.");
        utils.navigation.createNavi(player, "Mühle", true);
    }

    public void dropTransport(Player player) {
        if (locationManager.getDistanceBetweenCoords(player, "Mühle") < 5) {
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            int payout = Main.random(ServerManager.getPayout("weizenlieferant"), ServerManager.getPayout("weizenlieferant2"));
            player.sendMessage("§8[§eLieferant§8]§7 Danke für's abliefern. §a+" + payout + "$");
            SoundManager.successSound(player);
            playerManager.addExp(player, Main.random(1, 3));
            playerData.setIntVariable("weizen", playerData.getIntVariable("weizen") - 1);
            playerData.getScoreboard("weizen").updateWeizentransportScoreboard();
            try {
                playerManager.addBankMoney(player, payout, "Auszahlung Weizentransport");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (playerData.getIntVariable("weizen") <= 0) {
                player.sendMessage("§8[§eLieferant§8]§7 Du hast alles abgegeben. Danke!");
                playerData.setVariable("job", null);
                playerData.getScoreboard("weizen").killScoreboard();
                player.closeInventory();
            }
        } else {
            player.sendMessage(Main.error + "Du bist nicht in der nähe der Mühle.");
        }
    }
}
