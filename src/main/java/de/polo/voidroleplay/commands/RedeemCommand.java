package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RedeemCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public RedeemCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("redeem", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "redeem")) {
                try {
                    PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
                    Statement statement = Main.getInstance().mySQL.getStatement();
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
                        player.sendMessage(Prefix.MAIN + "§bDu hast dein §9" + args[0] + "§b eingelöst");
                        player.sendMessage(Prefix.MAIN + "§eDanke für's Unterstützen vom Server!");
                        switch (args[0].toLowerCase()) {
                            case "vip":
                                playerManager.redeemRank(player, "VIP", result.getInt(4), result.getString(5));
                                break;
                            case "premium":
                                playerManager.redeemRank(player, "Premium", result.getInt(4), result.getString(5));
                                break;
                            case "gold":
                                playerManager.redeemRank(player, "Gold", result.getInt(4), result.getString(5));
                                break;
                            case "hausslot":
                                utils.houseManager.addHausSlot(player);
                                break;
                            case "expbooster":
                                playerManager.addEXPBoost(player, result.getInt(4));
                                break;
                        }
                        playerManager.addExp(player, Main.random(20, 50));
                        statement.execute("DELETE FROM `payments` WHERE `id` = " + id);
                        statement.execute("INSERT INTO `payments_claimed` (`user`, `type`, `duration`, `duration_type`) VALUES ('" + player.getUniqueId() + "', '" + type + "', " + duration + ", '" + duration_type + "')");
                    } else {
                        player.sendMessage(Prefix.ERROR + "Dieser Kauf konnte nicht gefunden werden.");
                    }
                    Main.getInstance().getCooldownManager().setCooldown(player, "redeem", 5);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Warte noch einen Moment, bis du den Befehl ausführst...");
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /redeem [VIP/Premium/Gold/Hausslot/EXPBooster]");
        }
        return false;
    }
}
