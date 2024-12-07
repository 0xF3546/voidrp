package de.polo.voidroleplay.utils.playerUtils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.game.base.crypto.Miner;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.game.base.housing.HouseManager;
import de.polo.voidroleplay.game.base.vehicle.PlayerVehicleData;
import de.polo.voidroleplay.game.base.vehicle.VehicleData;
import de.polo.voidroleplay.game.base.vehicle.Vehicles;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.ServerManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
        double zinsen;
        if (playerData.getPermlevel() >= 20) {
            zinsen = playerManager.bank(player) * 0.014;
        } else {
            zinsen = playerManager.bank(player) * 0.010;
        }
        double steuern = 0;
        if (playerData.getRelationShip().containsValue("verheiratet")) {
            steuern = Math.round(playerManager.bank(player) * 0.0015);
        } else {
            steuern = Math.round(playerManager.bank(player) * 0.002);
        }
        int visumbonus = playerManager.visum(player) * 10;
        if (playerData.getPermlevel() >= 20) {
            visumbonus = visumbonus * 2;
        }
        int frakpayday = 0;
        plus = plus + zinsen - steuern + visumbonus;
        player.sendMessage("");
        player.sendMessage("§9======§6PayDay§9======");
        player.sendMessage("§9Alter Betrag§8: §6" + playerData.getBank() + "$");
        player.sendMessage("§7 » §9Zinsen§8:§a +" + (int) zinsen + "$");
        player.sendMessage("§7 » §9Steuern§8:§c -" + (int) steuern + "$");
        player.sendMessage("§7 » §9Sozialbonus§8:§a +" + visumbonus + "$");
        if (playerData.getBank() >= 100000) {
            double reichensteuer = Math.round(playerManager.bank(player) * 0.015);
            player.sendMessage("§7 » §9Reichensteuer§8:§c -" + (int) reichensteuer + "$");
            plus -= reichensteuer;
        }
        if (playerData.getFaction() != "Zivilist" && playerData.getFaction() != null) {
            frakpayday = factionManager.getPaydayFromFaction(playerData.getFaction(), playerData.getFactionGrade());
            if (factionManager.removeFactionMoney(playerData.getFaction(), frakpayday, "Gehalt " + player.getName())) {
                player.sendMessage("§§7 » §9Fraktionsgehalt§8: §a+" + frakpayday + "$");
                plus += frakpayday;
            }
        }
        int rent = 0;
        for (House houseData : HouseManager.houseDataMap.values()) {
            if (houseData.getRenter().get(player.getUniqueId().toString()) != null) {
                rent += houseData.getRenter().get(player.getUniqueId().toString());
                if (rent > playerData.getBank()) {
                    player.sendMessage("§7 » §cDu konntest deine Miete für Haus " + houseData.getNumber() + " nicht begleichen.");
                    continue;
                }
                player.sendMessage("§7 » §9Miete (Haus " + houseData.getNumber() + ")§8:§c -" + houseData.getRenter().get(player.getUniqueId().toString()) + "$");
                houseData.setMoney(houseData.getMoney() + houseData.getRenter().get(player.getUniqueId().toString()));
                houseData.setTotalMoney(houseData.getTotalMoney() + houseData.getRenter().get(player.getUniqueId().toString()));
                Main.getInstance().getMySQL().updateAsync("UPDATE `housing` SET `money` = ?, `totalMoney` = ? WHERE `number` = ?",
                        houseData.getMoney(),
                        houseData.getTotalMoney(),
                        houseData.getNumber());
                plus -= houseData.getRenter().get(player.getUniqueId().toString());
            }
            if (houseData.getOwner() != null) {
                if (houseData.getOwner().equals(player.getUniqueId().toString())) {
                    plus += houseData.getMoney();
                    Main.getInstance().getMySQL().updateAsync("UPDATE `housing` SET `money` = 0 WHERE `number` = ?", houseData.getNumber());
                    player.sendMessage("§7 » §9Mieteinnahmen (Haus " + houseData.getNumber() + ")§8: §a+" + houseData.getMoney() + "$");
                    houseData.setMoney(0);
                    if (houseData.isServerRoom()) {
                        float kWh = 0;
                        for (Miner miner : houseData.getActiveMiner()) {
                            kWh += miner.getKWh();
                            miner.setKWh(0);
                            miner.save();
                        }
                        float amount = kWh * ServerManager.getPayout("kwh");
                        player.sendMessage("§7 » §9Stromkosten (Haus " + houseData.getNumber() + ")§8: §c-" + amount + "$");
                        plus -= kWh;
                    }
                }
            }
        }

        if (playerData.isChurch()) {
            player.sendMessage("§7 » §9Kirchensteuer §8: §c-" + ServerManager.getPayout("kirchensteuer") + "$");
            plus -= ServerManager.getPayout("kirchensteuer");
            factionManager.addFactionMoney("Kirche", ServerManager.getPayout("kirchensteuer"), "Kirchensteuer " + player.getName());
        }
        if (playerData.getPermlevel() >= 40) {
            player.sendMessage("§7 » §9Team-Gehalt§8: §a" + playerData.getPermlevel() * ServerManager.getPayout("teamgehalt") + "$");
            plus += playerData.getPermlevel() * ServerManager.getPayout("teamgehalt");
        }
        if (playerData.getSecondaryTeam() != null) {
            player.sendMessage("§7 » §9Team-Gehalt§8: §a+" + ServerManager.getPayout("secondaryteam") + "$");
            plus += ServerManager.getPayout("secondaryteam");
        }
        for (PlayerVehicleData vehicleData : Vehicles.playerVehicleDataMap.values()) {
            if (vehicleData.getUuid().equals(player.getUniqueId().toString())) {
                VehicleData vehicleData1 = Vehicles.vehicleDataMap.get(vehicleData.getType());
                player.sendMessage("§7 » §9KFZ-Steuer (" + vehicleData.getType() + ")§8:§c -" + vehicleData1.getTax() + "$");
                plus -= vehicleData1.getTax();
            }
        }
        if (playerData.hasAnwalt()) {
            int anwalt = Main.random(15, 55);
            player.sendMessage("§7 » §9Anwaltskosten§8: §c-" + anwalt + "$");
            anwalt -= plus;
        }
        plus -= rent;
        plus = Math.round(plus);
        if (plus >= 0) {
            player.sendMessage("§9Neuer Betrag§8:§6 " + playerData.getBank() + "$§8(§a+" + (int) plus + "$§8)");
        } else {
            player.sendMessage("§9Neuer Betrag§8:§6 " + playerData.getBank() + "$§8(§c-" + (int) plus + "$§8)");
        }
        playerManager.addBankMoney(player, (int) plus, "PayDay");
        playerManager.addExp(player, Main.random(12, 20));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        if (playerData.getLastPayDay().getDayOfMonth() != LocalDateTime.now().getDayOfMonth()) {
            player.sendMessage("§8[§6Bonus§8]§7 Du kannst nun deine Daily-Case beim Bonushändler abholen.");
            Main.getInstance().seasonpass.didQuest(player, 6);
        }
        playerData.setLastPayDay(LocalDateTime.now());
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE players SET lastPayDay = NOW() WHERE uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        statement.execute();
        statement.close();
        connection.close();

        Main.getInstance().beginnerpass.didQuest(player, 6);
    }
}
