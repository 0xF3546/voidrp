package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.ContractData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

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
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /contract [Spieler] [Kopfgeld]");
            return false;
        }
        if (playerData.getFaction() != null) {
            if (playerData.getFaction().equalsIgnoreCase("ICA")) {
                player.sendMessage(Prefix.ERROR_NOPERMISSION);
                return false;
            }
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Prefix.ERROR + args[0] + " ist nicht online.");
            return false;
        }
        PlayerData targetplayerData = playerManager.getPlayerData(targetplayer);
        if (!Utils.getTime().isAfter(targetplayerData.getLastContract().plusHours(20))) {
            player.sendMessage(Prefix.ERROR + "Der Spieler hatte in den letzten 20 Stunden bereits Kopfgeld!");
            return false;
        }
        int price = Integer.parseInt(args[1]);
        if (price < ServerManager.getPayout("kopfgeld")) {
            player.sendMessage(Prefix.ERROR + "Die Mindestsumme beträgt " + ServerManager.getPayout("kopfgeld") + "$.");
            return false;
        }
        if (playerData.getBargeld() < price) {
            player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei.");
            return false;
        }
        targetplayerData.setLastContract(Utils.getTime());
        targetplayerData.save();
        if (factionManager.faction(targetplayer).equals("ICA")) {
            try {
                factionManager.addFactionMoney("ICA", price, "Versuchtes Kopfgeld auf Mitarbeiter");
                for (Player players : Bukkit.getOnlinePlayers()) {
                    PlayerData playersData = playerManager.getPlayerData(players);
                    if (playersData.getFaction() == null) continue;
                    if (playersData.getFaction().equalsIgnoreCase("ICA")) {
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
            factionManager.sendCustomMessageToFaction("ICA", "§8[§cKopfgeld§8]§7 Es wurde ein §eKopfgeld§7 in höhe von §a" + price + "$ §7auf §e" + targetplayer.getName() + "§7 gesetzt.");
            player.sendMessage("§8[§cKopfgeld§8]§7 Du hast ein §cKopfgeld§7 auf §c" + targetplayer.getName() + "§7 gesetzt.");
            try {
                playerManager.removeMoney(player, price, "Kopfgeld auf " + targetplayer.getName() + " gesetzt.");
                Main.getInstance().getMySQL().updateAsync("UPDATE contract SET amount = ? WHERE uuid = ?", contractData.getAmount(), targetplayer.getUniqueId().toString());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            ContractData contractData = new ContractData();
            contractData.setAmount(price);
            contractData.setUuid(targetplayer.getUniqueId().toString());
            contractData.setSetter(player.getUniqueId().toString());
            ServerManager.contractDataMap.put(targetplayer.getUniqueId().toString(), contractData);
            factionManager.sendCustomMessageToFaction("ICA", "§8[§cKopfgeld§8]§7 Es wurde ein §eKopfgeld§7 in höhe von §a" + price + "$ §7auf §e" + targetplayer.getName() + "§7 gesetzt.");
            player.sendMessage("§8[§cKopfgeld§8]§7 Du hast ein §cKopfgeld§7 auf §c" + targetplayer.getName() + "§7 gesetzt.");
            try {
                playerManager.removeMoney(player, price, "Kopfgeld auf " + targetplayer.getName() + " gesetzt.");
                Main.getInstance().getMySQL().insertAsync("INSERT INTO contract (uuid, amount, setter) VALUES (?, ?, ?)", targetplayer.getUniqueId().toString(), price, player.getUniqueId().toString());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }
}
