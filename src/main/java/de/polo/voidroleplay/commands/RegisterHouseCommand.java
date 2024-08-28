package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.*;

public class RegisterHouseCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    public RegisterHouseCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("registerhouse", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() >= 90) {
            if (args.length >= 1) {
                try {
                    Connection connection = Main.getInstance().mySQL.getConnection();
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO housing (number, price) VALUES (?, ?)");
                    statement.setInt(1, Integer.parseInt(args[0]));
                    statement.setInt(2, Integer.parseInt(args[1]));
                    statement.execute();
                    player.sendMessage(Main.gamedesign_prefix + "Haus " + args[0] + " wurde mit Preis " + args[1] + " angelegt.");
                    ResultSet result = statement.getGeneratedKeys();
                    if (result.next()) {
                        House house = new House(Integer.parseInt(args[0]), 2, 7);
                        house.setId(result.getInt(1));
                        house.setNumber(Integer.parseInt(args[0]));
                        house.setPrice(Integer.parseInt(args[1]));
                        Main.getInstance().housing.addHouse(house);
                    }
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                player.sendMessage(Main.gamedesign_prefix + "Syntax-Fehler: /registerhouse [Nummer] [Preis]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
