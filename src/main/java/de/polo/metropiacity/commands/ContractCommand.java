package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.ContractData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class ContractCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            if (args.length >= 2) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (targetplayer != null) {
                    int price = Integer.parseInt(args[1]);
                    if (price >= ServerManager.getPayout("kopfgeld")) {
                        if (playerData.getBargeld() >= price) {
                            if (ServerManager.contractDataMap.get(targetplayer.getUniqueId().toString()) != null) {
                                ContractData contractData = ServerManager.contractDataMap.get(targetplayer.getUniqueId().toString());
                                contractData.setAmount(contractData.getAmount() + price);
                                for (Player players : Bukkit.getOnlinePlayers()) {
                                    if (FactionManager.faction(players).equals("ICA")) {
                                        players.sendMessage("§8[§cKopfgeld§8]§7 Es wurde ein §eKopfgeld§7 in höhe von §a" + price + "$ §7auf §e" + targetplayer.getName() + "§7 gesetzt.");
                                    }
                                }
                                player.sendMessage("§8[§cKopfgeld§8]§7 Du hast ein §cKopfgeld§7 auf §c" + targetplayer.getName() + "§7 gesetzt.");
                                try {
                                    PlayerManager.removeMoney(player, price, "Kopfgeld auf " + targetplayer.getName() + " gesetzt.");
                                    Statement statement = MySQL.getStatement();
                                    statement.executeUpdate("UPDATE `contract` SET `amount` = " + contractData.getAmount() + " WHERE `uuid` = '" + targetplayer.getUniqueId() + "'");
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                ContractData contractData = new ContractData();
                                contractData.setAmount(price);
                                contractData.setUuid(targetplayer.getUniqueId().toString());
                                contractData.setSetter(player.getUniqueId().toString());
                                ServerManager.contractDataMap.put(targetplayer.getUniqueId().toString(), contractData);
                                for (Player players : Bukkit.getOnlinePlayers()) {
                                    if (FactionManager.faction(players).equals("ICA")) {
                                        players.sendMessage("§8[§cKopfgeld§8]§7 Es wurde ein §eKopfgeld§7 in höhe von §a" + price + "$ §7auf §e" + targetplayer.getName() + "§7 gesetzt.");
                                    }
                                }
                                player.sendMessage("§8[§cKopfgeld§8]§7 Du hast ein §cKopfgeld§7 auf §c" + targetplayer.getName() + "§7 gesetzt.");
                                try {
                                    PlayerManager.removeMoney(player, price, "Kopfgeld auf " + targetplayer.getName() + " gesetzt.");
                                    Statement statement = MySQL.getStatement();
                                    statement.execute("INSERT INTO `contract` (`uuid`, `amount`, `setter`) VALUES ('" + targetplayer.getUniqueId() + "', " + price + ", '" + player.getUniqueId() + "')");
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            player.sendMessage(Main.error + "Du hast nicht genug Geld dabei.");
                        }
                    } else {
                        player.sendMessage(Main.error + "Die Mindestsumme beträgt " + ServerManager.getPayout("kopfgeld") + "$.");
                    }
                } else {
                    player.sendMessage(Main.error + args[0] + " ist nicht online.");
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /contract [Spieler] [Kopfgeld]");
            }
        return false;
    }
}
