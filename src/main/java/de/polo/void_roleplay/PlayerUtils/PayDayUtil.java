package de.polo.void_roleplay.PlayerUtils;

import de.polo.void_roleplay.DataStorage.HouseData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.Housing;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

public class PayDayUtil {
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
        if (playerData.getBank() >= 300000) {
            double reichensteuer = Math.round(PlayerManager.bank(player) * 0.015);
            player.sendMessage("§8 ➥ §6Reichensteuer§8:§c" + (int) reichensteuer);
            plus -= reichensteuer;
        }
        plus -= rent;
        player.sendMessage(" ");
        plus = Math.round(plus);
        if (plus >= 0) {
            player.sendMessage("§8 ➥ §6Kontostand§8:§e " + new DecimalFormat("#,###").format(PlayerManager.bank(player)) + "$ §8(§a+" + (int) plus + "$§8)");
        } else {
            player.sendMessage("§8 ➥ §6Kontostand§8:§e " + new DecimalFormat("#,###").format(PlayerManager.bank(player)) + "$ §8(§c+" + (int) plus + "$§8)");
        }
        player.sendMessage(" ");
        PlayerManager.addBankMoney(player, (int) plus);
        PlayerManager.addExp(player, Main.random(12, 20));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }
}
