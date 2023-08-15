package de.polo.metropiacity.playerUtils;

import de.polo.metropiacity.dataStorage.HouseData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.dataStorage.PlayerVehicleData;
import de.polo.metropiacity.dataStorage.VehicleData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.*;
import de.polo.metropiacity.utils.Game.Housing;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

public class PayDayUtils {
    public static void givePayDay(Player player) throws SQLException {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        double plus = 0;
        double zinsen = Math.round(PlayerManager.bank(player) * 0.0075);
        double steuern = Math.round(PlayerManager.bank(player) * 0.0035);
        int visumbonus = PlayerManager.visum(player) * 10;
        int frakpayday = 0;
        plus = plus + zinsen - steuern + visumbonus;
        player.sendMessage("");
        player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §6Zinsen§8:§a +" + (int) zinsen + "$");
        player.sendMessage("§8 ➥ §6Steuern§8:§c -" + (int) steuern + "$");
        if (playerData.getBank() >= 300000) {
            double reichensteuer = Math.round(PlayerManager.bank(player) * 0.015);
            player.sendMessage("§8 ➥ §6Reichensteuer§8:§c -" + (int) reichensteuer + "$");
            plus -= reichensteuer;
        }
        player.sendMessage("§8 ➥ §6Sozialbonus§8:§a +" + visumbonus + "$");
        player.sendMessage(" ");
        if (playerData.getFaction() != "Zivilist" && playerData.getFaction() != null) {
            frakpayday = FactionManager.getPaydayFromFaction(playerData.getFaction(), playerData.getFactionGrade());
            if (FactionManager.removeFactionMoney(playerData.getFaction(), frakpayday, "Gehalt " + player.getName())) {
                player.sendMessage("§8 ➥ §6Gehalt [" + playerData.getFaction() + "]§8: §a+" + frakpayday + "$");
                plus += frakpayday;
            }
        }
        int rent = 0;
        for (HouseData houseData : Housing.houseDataMap.values()) {
            if (houseData.getRenter().get(player.getUniqueId().toString()) != null) {
                rent += houseData.getRenter().get(player.getUniqueId().toString());
                player.sendMessage("§8 ➥ §6Miete (Haus " + houseData.getNumber() + ")§8:§c -" + houseData.getRenter().get(player.getUniqueId().toString()) + "$");
                houseData.setMoney(houseData.getMoney() + houseData.getRenter().get(player.getUniqueId().toString()));
                houseData.setTotalMoney(houseData.getTotalMoney() + houseData.getRenter().get(player.getUniqueId().toString()));
                Statement statement = MySQL.getStatement();
                statement.executeUpdate("UPDATE `housing` SET `money` = " + houseData.getMoney() + ", `totalMoney` = " + houseData.getTotalMoney() + " WHERE `number` = " + houseData.getNumber());
            }
            if (houseData.getOwner() != null) {
                if (houseData.getOwner().equals(player.getUniqueId().toString())) {
                    plus += houseData.getMoney();
                    Statement statement = MySQL.getStatement();
                    statement.executeUpdate("UPDATE `housing` SET `money` = 0 WHERE `number` = " + houseData.getNumber());
                    player.sendMessage("§8 ➥ §6Mieteinnahmen (Haus " + houseData.getNumber() + ")§8: §a+" + houseData.getMoney() + "$");
                    houseData.setMoney(0);
                }
            }
        }
        if (playerData.getPermlevel() >= 40) {
            player.sendMessage("§8 ➥ §6Team-Gehalt (" + playerData.getRang() + ")§8: §a" + playerData.getPermlevel() * ServerManager.getPayout("teamgehalt") + "$");
            plus += playerData.getPermlevel() * ServerManager.getPayout("teamgehalt");
        }
        if (playerData.getSecondaryTeam() != null) {
            player.sendMessage("§8 ➥ §6Team-Gehalt (" + playerData.getSecondaryTeam() + ")§8: §a" + ServerManager.getPayout("secondaryteam") + "$");
            plus += ServerManager.getPayout("secondaryteam");
        }
        for (PlayerVehicleData vehicleData : Vehicles.playerVehicleDataMap.values()) {
            if (vehicleData.getUuid().equals(player.getUniqueId().toString())) {
                VehicleData vehicleData1 = Vehicles.vehicleDataMap.get(vehicleData.getType());
                player.sendMessage("§8 ➥ §6KFZ-Steuer (" + vehicleData.getType() + ")§8:§c -" + vehicleData1.getTax() + "$");
                plus -= vehicleData1.getTax();
            }
        }
        if (playerData.hasAnwalt()) {
            int anwalt = Main.random(15, 55);
            player.sendMessage(" ");
            player.sendMessage("§8 ➥ §6Anwaltskosten§8: §c-" + anwalt + "$");
            anwalt -= plus;
        }
        plus -= rent;
        player.sendMessage(" ");
        plus = Math.round(plus);
        if (plus >= 0) {
            player.sendMessage("§8 ➥ §6Kontostand§8:§e " + new DecimalFormat("#,###").format(PlayerManager.bank(player)) + "$ §7➡ §e" + new DecimalFormat("#,###").format(PlayerManager.bank(player) + (int) plus) + "$ §8(§a+" + (int) plus + "$§8)");
        } else {
            player.sendMessage("§8 ➥ §6Kontostand§8:§e " + new DecimalFormat("#,###").format(PlayerManager.bank(player)) + "$ §7➡ §e" + new DecimalFormat("#,###").format(PlayerManager.bank(player) + (int) plus) + "$ §8(§c" + (int) plus + "$§8)");
        }
        player.sendMessage(" ");
        PlayerManager.addBankMoney(player, (int) plus, "PayDay");
        PlayerManager.addExp(player, Main.random(12, 20));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }
}
