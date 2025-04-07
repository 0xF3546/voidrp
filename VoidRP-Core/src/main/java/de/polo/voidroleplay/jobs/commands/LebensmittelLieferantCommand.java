package de.polo.voidroleplay.jobs.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.VoidAPI;
import de.polo.voidroleplay.jobs.enums.MiniJob;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.location.services.impl.LocationManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class LebensmittelLieferantCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;

    public LebensmittelLieferantCommand(PlayerManager playerManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        Main.registerCommand("lebensmittellieferant", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = "§8[§aLieferant§8] §7";
        // ISSUE VPR-10003: The command should only be executable by players
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Prefix.ERROR + "Du musst ein Spieler sein.");
            return false;
        }
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getVariable("job") == null) {
            if (locationManager.getDistanceBetweenCoords(player, "lieferant") <= 5) {
                VoidAPI.getPlayer(player).setMiniJob(MiniJob.FOOD_SUPPLIER);
                player.sendMessage(prefix + "Du bist nun §aLebensmittel-Lieferant§7.");
                player.sendMessage(prefix + "Bringe die Lebensmittel zu einem Shop deiner Wahl!");
                playerData.setIntVariable("snacks", Utils.random(3, 7));
                playerData.setIntVariable("drinks", Utils.random(3, 7));
                    /*Scoreboard scoreboard = new Scoreboard(player);
                    scoreboard.createLebensmittelLieferantenScoreboard();
                    playerData.setScoreboard("lebensmittellieferant", scoreboard);*/
            } else {
                player.sendMessage(Prefix.ERROR + "Du bist §cnicht§7 in der nähe des §aLebensmittel-Lieferanten§7 Jobs!");
            }
        } else {
            if (playerData.getVariable("job").equals("lieferant")) {
                if (locationManager.getDistanceBetweenCoords(player, "lieferant") <= 5) {
                    player.sendMessage(prefix + "Du hast den Job Lebensmittel-Lieferant beendet.");
                    playerData.setVariable("job", null);
                    quitJob(player);
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Du übst bereits den Job " + playerData.getVariable("job") + " aus.");
            }
        }
        return false;
    }

    public void dropLieferung(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        int shop = locationManager.isNearShop(player);
        if (shop > 0) {
            int drinks = playerData.getIntVariable("drinks");
            int snacks = playerData.getIntVariable("snacks");
            int payout = (snacks * 4) + (drinks * 3);
            int exp = (snacks * 2) + (drinks * 3);
            playerManager.addExp(player, exp);
            quitJob(player);
            try {
                playerManager.addMoney(player, payout, "Lieferant");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            player.sendMessage("§aLieferant §8» §7Danke für die Lieferung! §a+" + payout + "$");
        } else {
            player.sendMessage(Prefix.ERROR + "Du bist bei keinem Shop.");
        }
    }

    public void quitJob(Player player) {
        VoidAPI.getPlayer(player).setMiniJob(null);
        Main.getInstance().beginnerpass.didQuest(player, 5);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        playerData.setIntVariable("drinks", null);
        playerData.setIntVariable("snacks", null);
        playerData.setVariable("job", null);
        //playerData.getScoreboard("lebensmittellieferant").killScoreboard();
    }
}
