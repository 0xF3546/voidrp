package de.polo.core.listeners;

import de.polo.core.Main;
import de.polo.api.VoidAPI;
import de.polo.core.admin.services.AdminService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.ServiceData;
import de.polo.core.storage.Ticket;
import de.polo.core.game.base.vehicle.Vehicles;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.gameplay.MilitaryDrop;
import de.polo.core.utils.StaatUtil;
import de.polo.core.utils.enums.PlayerPed;
import de.polo.core.utils.Event;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;

import static de.polo.core.Main.*;

@Event
public class QuitListener implements Listener {

    public QuitListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage("");
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData == null) return;
        scoreboardAPI.clearScoreboards(player);
        AdminService adminService = VoidAPI.getService(AdminService.class);
        adminService.send_message(player.getName() + " hat den Server verlassen.", Color.GRAY);
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            player.setGameMode(GameMode.SURVIVAL);
        }
        if (MilitaryDrop.ACTIVE) Main.getInstance().gamePlay.militaryDrop.handleQuit(player);
        serverManager.updateTablist(null);
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            playerData.setVariable("inventory::build", player.getInventory().getContents());
        }
        if (playerData.isDead()) {
            if (playerData.getVariable("inventory::base") != null) player.getInventory().setContents(playerData.getVariable("inventory::base"));
        }
        if (playerData.getVariable("ffa") != null) {
            Main.getInstance().gamePlay.getFfa().leaveFFA(player);
        }
        if (playerData.getVariable("gangwar") != null) {
            utils.gangwarUtils.leaveGangwar(player);
        }
        if (player.getVehicle() != null) {
            player.getVehicle().eject();
        }
        if (playerData.isCuffed()) {
            player.setWalkSpeed(0.2F);
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
            player.removePotionEffect(PotionEffectType.JUMP);
            player.removePotionEffect(PotionEffectType.SLOW);
        }
        PlayerPed ped = playerData.getPlayerPetManager().getActivePed();
        if (ped != null) {
            playerData.getPlayerPetManager().despawnPet(ped);
        }
        try {
            Vehicles.deleteVehicleByUUID(player.getUniqueId().toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            playerManager.savePlayer(player);
            Ticket ticket = supportManager.getTicket(player);
            if (ticket != null) {
                for (Player p : supportManager.getPlayersInTicket(ticket)) {
                    if (p.isOnline()) {
                        p.sendMessage(Prefix.SUPPORT + player.getName() + " hat den Server verlassen, das Ticket wurde geschlossen.");
                    }
                }
                supportManager.removeTicket(ticket);
            }
            ServiceData serviceData = StaatUtil.serviceDataMap.get(player.getUniqueId().toString());
            if (serviceData != null) {
                utils.staatUtil.cancelService(player);
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (player.getWorld() != p.getWorld()) continue;
                if (p.getLocation().distance(player.getLocation()) > 10) continue;
                p.sendMessage("§8 ➥ §7" + player.getName() + " hat den Server verlassen (" + p.getLocation().distance(player.getLocation()) + "m)");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        VoidAPI.removePlayer(player);
    }
}