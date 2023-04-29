package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.LocationManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class lebensmittellieferantCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = "§8[§aLieferant§8] §7";
        Player player = (Player) sender;
        String uuid = player.getUniqueId().toString();
        PlayerData playerData = PlayerManager.playerDataMap.get(uuid);
        if (playerData.canInteract()) {
            if (playerData.getVariable("job") == null) {
                if (LocationManager.getDistanceBetweenCoords(player, "lieferant") <= 5) {
                    playerData.setVariable("job", "lieferant");
                    player.sendMessage(prefix + "Du bist nun §aLebensmittel-Lieferant§7.");
                    player.sendMessage(prefix + "Bringe die Lebensmittel zu einem Shop deiner Wahl!");
                    playerData.setIntVariable("snacks", Main.random(3, 7));
                    playerData.setIntVariable("drinks", Main.random(3, 7));
                    playerData.getScoreboard().createLebensmittelLieferantenScoreboard();
                } else {
                    player.sendMessage(Main.error + "Du bist §cnicht§7 in der nähe des §aLebensmittel-Lieferanten§7 Jobs!");
                }
            } else {
                if (playerData.getVariable("job").equals("lieferant")) {
                    if (LocationManager.getDistanceBetweenCoords(player, "lieferant") <= 5) {
                        player.sendMessage(prefix + "Du hast den Job Lebensmittel-Lieferant beendet.");
                        playerData.setVariable("job", null);
                        quitJob(player);
                    }
                } else {
                    player.sendMessage(Main.error + "Du übst bereits den Job " + playerData.getVariable("job") + " aus.");
                }
            }
        } else {
            player.sendMessage(Main.error_cantinteract);
        }
        return false;
    }

    public static void dropLieferung(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        int shop = LocationManager.isNearShop(player);
        if (shop > 0) {
            int drinks = playerData.getIntVariable("drinks");
            int snacks = playerData.getIntVariable("snacks");
            int payout = (snacks * 4) + (drinks * 3);
            int exp = (snacks * 2) +(drinks * 3);
            PlayerManager.addExp(player, exp);
            quitJob(player);
            try {
                PlayerManager.addMoney(player, payout);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            player.sendMessage("§aLieferant §8» §7Danke für die Lieferung! §a+" + payout + "$");
        } else {
            player.sendMessage(Main.error + "Du bist bei keinem Shop.");
        }
    }

    public static void quitJob(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setIntVariable("drinks", null);
        playerData.setIntVariable("snacks", null);
        playerData.setVariable("job", null);
        playerData.getScoreboard().killScoreboard();
    }
}
