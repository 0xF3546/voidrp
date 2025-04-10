package de.polo.core.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.game.base.shops.ShopData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.location.services.impl.LocationManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.shop.services.ShopService;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.player.Progress;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;

public class ShopRobCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    private final FactionManager factionManager;

    public ShopRobCommand(PlayerManager playerManager, LocationManager locationManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.factionManager = factionManager;
        Main.registerCommand("shoprob", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        int shopId = locationManager.isNearShop(player);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getVisum() < 3) {
            player.sendMessage(Prefix.ERROR + "Du kannst erst mit Visum 3 Shops ausrauben!");
            return false;
        }
        if (shopId == 0) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe eines Shops.");
            return false;
        }
        int count = factionManager.getOnlineMemberCount("Polizei");
        count += factionManager.getOnlineMemberCount("FBI");
        if (count < 2) {
            player.sendMessage(Component.text(Prefix.ERROR + "Es sind nicht genug Beamte online."));
            return false;
        }
        if (ServerManager.serverVariables.get("shoprob") != null) {
            player.sendMessage(Prefix.ERROR + "Es ist bereits ein Shoprob im Gange");
            return false;
        }
        if (Main.getInstance().getCooldownManager().isOnCooldown(player, "shoprob")) {
            player.sendMessage(Prefix.ERROR + "Du kannst in " + Utils.getTime(Main.getInstance().getCooldownManager().getRemainingTime(player, "shoprob")) + " wieder einen Shop ausrauben.");
            return false;
        }
        if (Main.getInstance().getCooldownManager().isOnStringCooldown("shop_" + shopId, "shoprob")) {
            player.sendMessage(Prefix.ERROR + "Dieser Shop kann erst in " + Utils.getTime(Main.getInstance().getCooldownManager().getRemainingStringTime("shop_" + shopId, "shoprob")) + " wieder ausgeraubt werden.");
            return false;
        }
        player.sendMessage("§8[§cShoprob§8]§7 Du fängst an den Shop auszurauben, warte 60 Sekunden!");
        player.sendMessage("§b   Info:§f Du bekommst dann jede Minute Geld, bis der Shop leer ist.");
        Main.getInstance().getCooldownManager().setGlobalCooldown("shop_" + shopId, "shoprob", 3600);
        ServerManager.setVariable("shoprob", "isRob");
        ShopService service = VoidAPI.getService(ShopService.class);
        for (ShopData shopData : service.getShops()) {
            if (shopData.getId() == shopId) {
                factionManager.sendMessageToFaction("Polizei", "Es wurde ein Shoprob bei \"" + shopData.getName() + "\" gemeldet!");
                factionManager.sendMessageToFaction("FBI", "Es wurde ein Shoprob bei \"" + shopData.getName() + "\" gemeldet!");
                ServerManager.setVariable("shoprob_payout", "0");
                Utils.waitSeconds(60, () -> {
                    if (player.isOnline()) {
                        if (locationManager.isNearShop(player) == 0) {
                            player.sendMessage("§8[§cShoprob§8]§c Der Shoprob ist fehlgeschlagen!");
                            ServerManager.setVariable("shoprob", null);
                            factionManager.sendMessageToFaction("Polizei", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                            factionManager.sendMessageToFaction("FBI", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                            return;
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (player.isOnline()) {
                                    if (locationManager.isNearShop(player) == 0) {
                                        player.sendMessage("§8[§cShoprob§8]§c Der Shoprob ist fehlgeschlagen!");
                                        ServerManager.setVariable("shoprob", null);
                                        factionManager.sendMessageToFaction("Polizei", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                                        factionManager.sendMessageToFaction("FBI", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                                        cancel();
                                    }
                                    if (Integer.parseInt(ServerManager.getVariable("shoprob_payout")) < ServerManager.getPayout("maxShopRobPayout")) {
                                        try {
                                            Progress.start(player, 60);
                                            int payout = Utils.random(80, 120);
                                            playerManager.addMoney(player, payout, "Shoprob");
                                            player.sendMessage("§8[§cShoprob§8]§a +" + payout + "$");
                                            ServerManager.setVariable("shoprob_payout", String.valueOf(Integer.parseInt(ServerManager.getVariable("shoprob_payout")) + payout));
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        player.sendMessage("§8[§cShoprob§8]§a Du hast alles erbeutet!");
                                        ServerManager.setVariable("shoprob", null);
                                        factionManager.sendMessageToFaction("Polizei", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                                        factionManager.sendMessageToFaction("FBI", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                                        cancel();
                                    }
                                } else {
                                    ServerManager.setVariable("shoprob", null);
                                    factionManager.sendMessageToFaction("Polizei", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                                    factionManager.sendMessageToFaction("FBI", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                                    cancel();
                                }
                            }
                        }.runTaskTimer(Main.getInstance(), 20 * 2, 20 * 60);

                    } else {
                        ServerManager.setVariable("shoprob", null);
                        factionManager.sendMessageToFaction("Polizei", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                        factionManager.sendMessageToFaction("FBI", "Der Shoprob bei \"" + shopData.getName() + "\" ist beendet!");
                    }
                });
            }
        }
        return false;
    }
}
