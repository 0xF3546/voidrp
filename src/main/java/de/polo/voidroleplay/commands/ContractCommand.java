package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.ContractData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class ContractCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    public ContractCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("contract", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!(args.length >= 2)) {
            player.sendMessage(Main.error + "Syntax-Fehler: /contract [Spieler] [Kopfgeld]");
            return false;
        }
        if (playerData.getFaction().equalsIgnoreCase("ICA")) {
            player.sendMessage(Prefix.error_nopermission);
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Main.error + args[0] + " ist nicht online.");
            return false;
        }
        int price = Integer.parseInt(args[1]);
        if (price < ServerManager.getPayout("kopfgeld")) {
            player.sendMessage(Main.error + "Die Mindestsumme beträgt " + ServerManager.getPayout("kopfgeld") + "$.");
            return false;
        }
        if (playerData.getBargeld() < price) {
            player.sendMessage(Main.error + "Du hast nicht genug Geld dabei.");
            return false;
        }
        if (factionManager.faction(targetplayer).equals("ICA")) {
            try {
                factionManager.addFactionMoney("ICA", price, "Versuchtes Kopfgeld auf Mitarbeiter");
                for (Player players : Bukkit.getOnlinePlayers()) {
                    PlayerData playersData = playerManager.getPlayerData(players);
                    if (playersData.getFaction() == null) continue;
                    if (playersData.getFaction().equals("ICA")) {
                        players.sendMessage("§8[§cKopfgeld§8]§7 Es wurde versucht ein Kopfgeld auf einen Mitarbeiter der ICA zu setzen. Das Kopfgeld wurde auf das Fraktionskonto überschrieben.");
                    }
                }
                playerManager.removeMoney(player, price, "Kopfgeld auf " + targetplayer.getName() + " gesetzt.");
                player.sendMessage("§8[§cKopfgeld§8]§7 Du hast ein §cKopfgeld§7 auf §c" + targetplayer.getName() + "§7 gesetzt.");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
        if (ServerManager.contractDataMap.get(targetplayer.getUniqueId().toString()) != null) {
            ContractData contractData = ServerManager.contractDataMap.get(targetplayer.getUniqueId().toString());
            contractData.setAmount(contractData.getAmount() + price);
            for (Player players : Bukkit.getOnlinePlayers()) {
                PlayerData playersData = playerManager.getPlayerData(players);
                if (playersData.getFaction() == null) continue;
                if (playersData.getFaction().equals("ICA")) {
                    players.sendMessage("§8[§cKopfgeld§8]§7 Es wurde ein §eKopfgeld§7 in höhe von §a" + price + "$ §7auf §e" + targetplayer.getName() + "§7 gesetzt.");
                }
            }
            player.sendMessage("§8[§cKopfgeld§8]§7 Du hast ein §cKopfgeld§7 auf §c" + targetplayer.getName() + "§7 gesetzt.");
            try {
                playerManager.removeMoney(player, price, "Kopfgeld auf " + targetplayer.getName() + " gesetzt.");
                Statement statement = Main.getInstance().mySQL.getStatement();
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
                PlayerData playersData = playerManager.getPlayerData(player);
                if (playersData.getFaction() == null) continue;
                if (playersData.getFaction().equals("ICA")) {
                    players.sendMessage("§8[§cKopfgeld§8]§7 Es wurde ein §eKopfgeld§7 in höhe von §a" + price + "$ §7auf §e" + targetplayer.getName() + "§7 gesetzt.");
                }
            }
            player.sendMessage("§8[§cKopfgeld§8]§7 Du hast ein §cKopfgeld§7 auf §c" + targetplayer.getName() + "§7 gesetzt.");
            try {
                playerManager.removeMoney(player, price, "Kopfgeld auf " + targetplayer.getName() + " gesetzt.");
                Statement statement = Main.getInstance().mySQL.getStatement();
                statement.execute("INSERT INTO `contract` (`uuid`, `amount`, `setter`) VALUES ('" + targetplayer.getUniqueId() + "', " + price + ", '" + player.getUniqueId() + "')");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }
}
