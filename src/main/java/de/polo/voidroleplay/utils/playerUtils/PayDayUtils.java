package de.polo.voidroleplay.utils.playerUtils;

import de.polo.voidroleplay.dataStorage.HouseData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.PlayerVehicleData;
import de.polo.voidroleplay.dataStorage.VehicleData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.Game.Housing;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.LocalDateTime;

public class PayDayUtils {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    public PayDayUtils(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
    }
    public void givePayDay(Player player) throws SQLException {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        double plus = 0;
        double zinsen = Math.round(playerManager.bank(player) * 0.0075);
        double steuern = Math.round(playerManager.bank(player) * 0.0035);
        int visumbonus = playerManager.visum(player) * 10;
        int frakpayday = 0;
        plus = plus + zinsen - steuern + visumbonus;
        player.sendMessage("");
        player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §6Zinsen§8:§a +" + (int) zinsen + "$");
        player.sendMessage("§8 ➥ §6Steuern§8:§c -" + (int) steuern + "$");
        if (playerData.getBank() >= 300000) {
            double reichensteuer = Math.round(playerManager.bank(player) * 0.015);
            player.sendMessage("§8 ➥ §6Reichensteuer§8:§c -" + (int) reichensteuer + "$");
            plus -= reichensteuer;
        }
        player.sendMessage("§8 ➥ §6Sozialbonus§8:§a +" + visumbonus + "$");
        player.sendMessage(" ");
        if (playerData.getFaction() != "Zivilist" && playerData.getFaction() != null) {
            frakpayday = factionManager.getPaydayFromFaction(playerData.getFaction(), playerData.getFactionGrade());
            if (factionManager.removeFactionMoney(playerData.getFaction(), frakpayday, "Gehalt " + player.getName())) {
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
                Statement statement = Main.getInstance().mySQL.getStatement();
                statement.executeUpdate("UPDATE `housing` SET `money` = " + houseData.getMoney() + ", `totalMoney` = " + houseData.getTotalMoney() + " WHERE `number` = " + houseData.getNumber());
            }
            if (houseData.getOwner() != null) {
                if (houseData.getOwner().equals(player.getUniqueId().toString())) {
                    plus += houseData.getMoney();
                    Statement statement = Main.getInstance().mySQL.getStatement();
                    statement.executeUpdate("UPDATE `housing` SET `money` = 0 WHERE `number` = " + houseData.getNumber());
                    player.sendMessage("§8 ➥ §6Mieteinnahmen (Haus " + houseData.getNumber() + ")§8: §a+" + houseData.getMoney() + "$");
                    houseData.setMoney(0);
                }
            }
        }
        if (playerData.getPermlevel() >= 40) {
            player.sendMessage("§8 ➥ §6Team-Gehalt (" + playerData.getRang() + ")§8: §a" + playerData.getPermlevel() * Main.getInstance().serverManager.getPayout("teamgehalt") + "$");
            plus += playerData.getPermlevel() * Main.getInstance().serverManager.getPayout("teamgehalt");
        }
        if (playerData.getSecondaryTeam() != null) {
            player.sendMessage("§8 ➥ §6Team-Gehalt (" + playerData.getSecondaryTeam() + ")§8: §a+" + Main.getInstance().serverManager.getPayout("secondaryteam") + "$");
            plus += Main.getInstance().serverManager.getPayout("secondaryteam");
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
            player.sendMessage("§8 ➥ §6Kontostand§8:§e " + new DecimalFormat("#,###").format(playerManager.bank(player)) + "$ §7➡ §e" + new DecimalFormat("#,###").format(playerManager.bank(player) + (int) plus) + "$ §8(§a+" + (int) plus + "$§8)");
        } else {
            player.sendMessage("§8 ➥ §6Kontostand§8:§e " + new DecimalFormat("#,###").format(playerManager.bank(player)) + "$ §7➡ §e" + new DecimalFormat("#,###").format(playerManager.bank(player) + (int) plus) + "$ §8(§c" + (int) plus + "$§8)");
        }
        player.sendMessage(" ");
        playerManager.addBankMoney(player, (int) plus, "PayDay");
        playerManager.addExp(player, Main.random(12, 20));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        if (playerData.getLastPayDay().getDayOfMonth() != LocalDateTime.now().getDayOfMonth()) {
            player.sendMessage(" ");
            player.sendMessage("§8[§6Bonus§8]§7 Du kannst nun deine Daily-Case beim Bonushändler abholen.");
        }
        playerData.setLastPayDay(LocalDateTime.now());
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE players SET lastPayDay = NOW() WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        statement.execute();
        statement.close();
        connection.close();
    }
}
