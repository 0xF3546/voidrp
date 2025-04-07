package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.NaviData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.NavigationManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class SperrzoneCommand implements CommandExecutor {
    public static final List<String> sperrzonen = new ObjectArrayList<>();
    private final PlayerManager playerManager;
    private final LocationManager locationManager;

    public SperrzoneCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;

        Main.registerCommand("sperrzone", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.getFaction().equalsIgnoreCase("FBI") && !playerData.getFaction().equalsIgnoreCase("Polizei")) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        NaviData naviData = NavigationManager.getNearestNaviPoint(player.getLocation());
        Location location = locationManager.getLocation(naviData.getLocation());
        for (String point : sperrzonen) {
            if (args.length >= 1) {
                if (Utils.stringArrayToString(args).replace("&", "§").equalsIgnoreCase(point)) {
                    removeSperrzone(point);
                    return false;
                }
            }
        }
        if (args.length == 1) {
            try {
                if (location.distance(player.getLocation()) > 50) {
                    player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe eines Navi-Punktes. Nutze /sperrzone [Reichweite] [Punkt]");
                    return false;
                }
                int range = Integer.parseInt(args[0]);
                addSperrzone(naviData.getName(), range);
            } catch (Exception e) {
                player.sendMessage(Prefix.ERROR + "Die Reichweite muss numerisch sein.");
            }
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /sperrzone [Reichweite] [(Sperrzone)]");
            return false;
        }
        try {
            int range = Integer.parseInt(args[0]);
            args[0] = "";
            addSperrzone(Utils.stringArrayToString(args), range);
        } catch (Exception e) {
            player.sendMessage(Prefix.ERROR + "Die Reichweite muss numerisch sein.");
        }
        return false;
    }

    private void removeSperrzone(String point) {
        Bukkit.broadcastMessage("§7§m===§8[§9Sperrzone§8]§7§m===");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8 ➥ §bDie Sperrzone um " + point.replace("&", "§").replace("_", " ") + " §bwurde hiermit aufgehoben. Das Gebiet kann nun wieder betreten werden.");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§7§m===§8[§9Sperrzone§8]§7§m===");
        sperrzonen.remove(point.replace(" ", "_").replace("&", "§"));
    }

    private void addSperrzone(String point, int range) {
        Bukkit.broadcastMessage("§7§m===§8[§9Sperrzone§8]§7§m===");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§8 ➥ §bEs wurde eine Sperrzone um " + point.replace("&", "§").replace("_", " ") + " §bim Umkreis von " + range + " Metern ausgerufen. Jegliches betreten dieser Sperrzone ist ein Straftatbestand und wird geahndet.");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§7§m===§8[§9Sperrzone§8]§7§m===");
        sperrzonen.add(point.replace(" ", "_").replace("&", "§"));
    }
}
