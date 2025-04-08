package de.polo.core.faction.commands;

import de.polo.core.Main;
import de.polo.core.storage.ContractData;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.utils.Prefix;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

public class ContractsCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public ContractsCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("contracts", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFaction().equals("ICA")) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("all")) {
                    player.sendMessage("§7   ===§8[§cAlle Kopfgelder§8]§7===");
                    for (ContractData contractData : ServerManager.contractDataMap.values()) {
                        OfflinePlayer offPLayer = Bukkit.getOfflinePlayer(UUID.fromString(contractData.getUuid()));
                        player.sendMessage("§8 ➥ §e" + offPLayer.getName() + "§8 | §e" + contractData.getAmount() + "$");
                    }
                }
                if (args[0].equalsIgnoreCase("remove")) {
                    if (args.length >= 2) {
                        Player targetplayer = Bukkit.getPlayer(args[1]);
                        if (targetplayer != null) {
                            if (ServerManager.contractDataMap.get(targetplayer.getUniqueId().toString()) != null) {
                                if (playerData.getFactionGrade() >= 4) {
                                    try {
                                        if (factionManager.removeFactionMoney(playerData.getFaction(), ServerManager.contractDataMap.get(targetplayer.getUniqueId().toString()).getAmount(), "Kopfgeld entfernung durch " + player.getName())) {
                                            for (Player players : Bukkit.getOnlinePlayers()) {
                                                PlayerData pdata = playerManager.getPlayerData(players);
                                                if (pdata.getFaction() == null) continue;
                                                if (pdata.getFaction().equals("ICA")) {
                                                    players.sendMessage("§8[§cKopfgeld§8]§e " + factionManager.getPlayerFactionRankName(player) + " " + player.getName() + " §7hat das Kopfgeld von §e" + targetplayer.getName() + " §7gelöscht.");
                                                }
                                            }
                                            ServerManager.contractDataMap.remove(targetplayer.getUniqueId().toString());
                                            try {
                                                Statement statement = Main.getInstance().coreDatabase.getStatement();
                                                statement.execute("DELETE FROM `contract` WHERE `uuid` = '" + targetplayer.getUniqueId() + "'");
                                            } catch (SQLException e) {
                                                throw new RuntimeException(e);
                                            }
                                        } else {
                                            player.sendMessage(Prefix.ERROR + "Deine Fraktion kann das Kopfgeld nicht zahlen.");
                                        }
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    player.sendMessage(Prefix.ERROR_NOPERMISSION);
                                }
                            } else {
                                player.sendMessage(Prefix.ERROR + targetplayer.getName() + " hat kein Kopfgeld.");
                            }
                        } else {
                            player.sendMessage(Prefix.ERROR + args[1] + " ist nicht online!");
                        }
                    } else {
                        player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /contracts remove [Spieler]");
                    }
                }
            } else {
                player.sendMessage("§7   ===§8[§cKopfgelder§8]§7===");
                for (ContractData contractData : ServerManager.contractDataMap.values()) {
                    OfflinePlayer offPLayer = Bukkit.getOfflinePlayer(UUID.fromString(contractData.getUuid()));
                    if (offPLayer.isOnline()) {
                        player.sendMessage("§8 ➥ §e" + offPLayer.getName() + "§8 | §e" + contractData.getAmount() + "$");
                    }
                }
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ObjectArrayList<>();
            suggestions.add("all");
            suggestions.add("remove");

            return suggestions;
        }
        return null;
    }
}
