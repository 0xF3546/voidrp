package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.DataStorage.ShopData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.PlayerUtils.progress;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.LocationManager;
import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.ServerManager;
import jdk.internal.net.http.common.Utils;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;

public class shoprobCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        int shopId = LocationManager.isNearShop(player);
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getVisum() < 3) {
            player.sendMessage(Main.error + "Du kannst erst mit Visum 2 Shops ausrauben!");
            return false;
        }
        if (shopId == 0) {
            player.sendMessage(Main.error + "Du bist nicht in der nähe eines Shops.");
            return false;
        }
        if (ServerManager.serverVariables.get("shoprob") != null) {
            player.sendMessage(Main.error + "Es ist bereits in Shoprob im gange.");
            return false;
        }
        if (Main.cooldownManager.isOnCooldown(player, "shoprob")) {
            player.sendMessage(Main.error + "Du kannst in " + Main.getTime(Main.cooldownManager.getRemainingTime(player, "shoprob")) + " wieder einen Shop ausrauben.");
            return false;
        }
        if (Main.cooldownManager.isOnStringCooldown("shop_" + shopId, "shoprob")) {
            player.sendMessage(Main.error + "Dieser Shop kann erst in " + Main.getTime(Main.cooldownManager.getRemainingStringTime("shop_" + shopId, "shoprob")) + " wieder ausgeraubt werden.");
            return false;
        }
        player.sendMessage("§8[§cShoprob§8]§7 Du fängst an den Shop auszurauben, warte 60 Sekunden!");
        player.sendMessage("§b   Info:§f Du bekommst dann jede Minute Geld, bis der Shop leer ist.");
        Main.cooldownManager.setStringCooldown("shop_" + shopId, "shoprob", 3600);
        ServerManager.setVariable("shoprob", "isRob");
        for (ShopData shopData : ServerManager.shopDataMap.values()) {
            if (shopData.getId() == shopId) {
                FactionManager.sendMessageToFaction("Polizei", "Es wurde ein Shoprob bei \"" + shopData.getName() + "\" gemeldet!");
                FactionManager.sendMessageToFaction("FBI", "Es wurde ein Shoprob bei \"" + shopData.getName() + "\" gemeldet!");
                ServerManager.setVariable("shoprob_payout", "0");
                Main.waitSeconds(60, () -> {
                    if (player.isOnline()) {
                        if (LocationManager.isNearShop(player) == 0) {
                            player.sendMessage("§8[§cShoprob§8]§c Der Shoprob ist fehlgeschlagen!");
                            ServerManager.setVariable("shoprob", null);
                            FactionManager.sendMessageToFaction("Polizei", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                            FactionManager.sendMessageToFaction("FBI", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                            return;
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (player.isOnline()) {
                                    if (LocationManager.isNearShop(player) == 0) {
                                        player.sendMessage("§8[§cShoprob§8]§c Der Shoprob ist fehlgeschlagen!");
                                        ServerManager.setVariable("shoprob", null);
                                        FactionManager.sendMessageToFaction("Polizei", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                                        FactionManager.sendMessageToFaction("FBI", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                                        cancel();
                                    }
                                    if (Integer.parseInt(ServerManager.getVariable("shoprob_payout")) < ServerManager.getPayout("maxShopRobPayout")) {
                                        try {
                                            progress.start(player, 60);
                                            int payout = Main.random(80, 120);
                                            PlayerManager.addMoney(player, payout);
                                            player.sendMessage("§8[§cShoprob§8]§a +" + payout + "$");
                                            ServerManager.setVariable("shoprob_payout", String.valueOf(Integer.parseInt(ServerManager.getVariable("shoprob_payout")) + payout));
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        player.sendMessage("§8[§cShoprob§8]§a Du hast alles erbeutet!");
                                        ServerManager.setVariable("shoprob", null);
                                        FactionManager.sendMessageToFaction("Polizei", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                                        FactionManager.sendMessageToFaction("FBI", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                                        cancel();
                                    }
                                } else {
                                    ServerManager.setVariable("shoprob", null);
                                    FactionManager.sendMessageToFaction("Polizei", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                                    FactionManager.sendMessageToFaction("FBI", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                                    cancel();
                                }
                            }
                        }.runTaskTimer(Main.getInstance(), 20 * 2, 20 * 60);

                    } else {
                        ServerManager.setVariable("shoprob", null);
                        FactionManager.sendMessageToFaction("Polizei", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                        FactionManager.sendMessageToFaction("FBI", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                    }
                });
            }
        }
        return false;
    }
}
