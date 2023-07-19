package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.Utils.Housing;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class redeemCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            if (!Main.cooldownManager.isOnCooldown(player, "redeem")) {
                try {
                    PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
                    Statement statement = MySQL.getStatement();
                    ResultSet result = statement.executeQuery("SELECT * FROM `payments` WHERE `user` = '" + player.getUniqueId().toString().replace("-", "") + "' AND type = '" + args[0].toLowerCase() + "'");
                    System.out.println(args[0]);
                    int id;
                    String type;
                    int duration;
                    String duration_type;
                    if (result.next()) {
                        id = result.getInt(1);
                        type = result.getString(3);
                        duration = result.getInt(4);
                        duration_type = result.getString(5);
                        player.sendMessage(Main.prefix + "§bDu hast dein §9" + args[0] + "§b eingelöst");
                        player.sendMessage(Main.prefix + "§eDanke für's Unterstützen von Void Roleplay!");
                        switch (args[0]) {
                            case "vip":
                                PlayerManager.redeemRank(player, "VIP", result.getInt(4), result.getString(5));
                                break;
                            case "premium":
                                PlayerManager.redeemRank(player, "Premium", result.getInt(4), result.getString(5));
                                break;
                            case "gold":
                                PlayerManager.redeemRank(player, "Gold", result.getInt(4), result.getString(5));
                                break;
                            case "hausslot":
                                Housing.addHausSlot(player);
                                break;
                            case "expbooster":
                                PlayerManager.addEXPBoost(player, result.getInt(4));
                                break;
                        }
                        PlayerManager.addExp(player, Main.random(20, 50));
                        statement.execute("DELETE FROM `payments` WHERE `id` = " + id);
                        statement.execute("INSERT INTO `payments_claimed` (`user`, `type`, `duration`, `duration_type`) VALUES ('" + player.getUniqueId() + "', '" + type + "', " + duration + ", '" + duration_type + "')");
                    } else {
                        player.sendMessage(Main.error + "Dieser Kauf konnte nicht gefunden werden.");
                    }
                    Main.cooldownManager.setCooldown(player, "redeem", 5);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                player.sendMessage(Main.error + "Warte noch einen Moment, bis du den Befehl ausführst...");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /redeem [VIP/Premium/Gold/Hausslot/EXPBooster]");
        }
        return false;
    }
}
