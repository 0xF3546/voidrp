package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.BlacklistData;
import de.polo.void_roleplay.DataStorage.FactionData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.MySQl.MySQL;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

public class blacklistCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFaction() != null && playerData.getFaction() != "Zivilist") {
            FactionData factionData = FactionManager.factionDataMap.get(playerData.getFaction());
            if (factionData.hasBlacklist()) {
                if (args.length == 0) {
                    player.sendMessage("§7   ===§8[§cBlacklist§8]§7===");
                    player.sendMessage("§8 ➥ §" + factionData.getPrimaryColor() + factionData.getFullname());
                    player.sendMessage(" ");
                    for (BlacklistData blacklistData : FactionManager.blacklistDataMap.values()) {
                        if (blacklistData.getFaction().equals(factionData.getName())) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(blacklistData.getUuid()));
                            player.sendMessage("§8 ➥ §e" + offlinePlayer.getName() + "§8 | §e" + blacklistData.getPrice() + "$ §8 | §e" + blacklistData.getKills() + " Tode §8| §e" + blacklistData.getReason() + " §8|§e " + blacklistData.getDate());
                        }
                    }
                } else {
                    if (args.length >= 1) {
                        if (args.length >= 4) {
                            if (args[0].equalsIgnoreCase("add")) {
                                Player player1 = Bukkit.getPlayer(args[1]);
                                if (player1.isOnline()) {
                                    if (playerData.getFactionGrade() >= 3) {
                                        boolean canDo = true;
                                        for (BlacklistData blacklistData : FactionManager.blacklistDataMap.values()) {
                                            if (blacklistData.getFaction().equals(playerData.getFaction())) {
                                                if (Objects.equals(blacklistData.getUuid(), player1.getUniqueId().toString())) {
                                                    canDo = false;
                                                    break;
                                                }
                                            }
                                        }
                                        if (canDo) {
                                            int price = Integer.parseInt(args[2]);
                                            int kills = Integer.parseInt(args[3]);
                                            String reason = "";
                                            for (int i = 4; i < args.length; i++) {
                                                reason = reason + " " + args[i];
                                            }
                                            try {
                                                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyy '|' HH:mm:ss ");
                                                Date date = new Date();
                                                String newDate = formatter.format(date);
                                                Statement statement = MySQL.getStatement();
                                                statement.execute("INSERT INTO `blacklist` (`uuid`, `faction`, `kills`, `price`, `date`, `reason`) VALUES ('" + player1.getUniqueId().toString() + "', '" + factionData.getName() + "', " + kills + ", " + price + ", '" + newDate + "', '" + reason + "')");
                                                ResultSet checkId = statement.executeQuery("SELECT `id` FROM `blacklist` WHERE `uuid` = '" + player1.getUniqueId().toString() + "' AND `date` = '" + newDate + "'");
                                                if (checkId.next()) {
                                                    BlacklistData blacklistData = new BlacklistData();
                                                    blacklistData.setDate(newDate);
                                                    blacklistData.setPrice(price);
                                                    blacklistData.setReason(reason);
                                                    blacklistData.setKills(kills);
                                                    blacklistData.setFaction(factionData.getName());
                                                    blacklistData.setId(checkId.getInt(1));
                                                    blacklistData.setUuid(player1.getUniqueId().toString());
                                                    FactionManager.blacklistDataMap.put(checkId.getInt(1), blacklistData);
                                                }
                                                FactionManager.sendMessageToFaction(factionData.getName(), FactionManager.getPlayerFactionRankName(player) + " " + player.getName() + " hat " + player1.getName() + " auf die Blacklist gesetzt.");
                                                player1.sendMessage("§8[§cBlacklist§8]§c Du wurdest auf die Blacklist von " + factionData.getFullname() + " gesetzt.");
                                                player1.sendMessage("§8[§cBlacklist§8]§c " + kills + " Kills §8| §c" + price + "$§8 | §c" + reason);
                                            } catch (SQLException e) {
                                                player.sendMessage(Main.error + "Bitte versuche es später erneut.");
                                                throw new RuntimeException(e);
                                            }
                                        } else {
                                            player.sendMessage(Main.error + player1.getName() + " ist bereits auf der Blacklist.");
                                        }
                                    } else {
                                        player.sendMessage(Main.error + "Dieser Befehl ist erst ab Rang 3+ verfübar.");
                                    }
                                } else {
                                    player.sendMessage(Main.error + args[0] + " ist nicht online.");
                                }
                            }
                        } else if (args[0].equalsIgnoreCase("remove")) {
                            Player player1 = Bukkit.getPlayer(args[1]);
                            if (player1.isOnline()) {
                                if (playerData.getFactionGrade() >= 5) {
                                    boolean canDo = false;
                                    for (BlacklistData blacklistData : FactionManager.blacklistDataMap.values()) {
                                        if (blacklistData.getUuid().equals(player1.getUniqueId().toString()) && blacklistData.getFaction().equals(factionData.getName())) {
                                            canDo = true;

                                            try {
                                                Statement statement = MySQL.getStatement();
                                                statement.execute("DELETE FROM `blacklist` WHERE `id` = " + blacklistData.getId());
                                                FactionManager.sendMessageToFaction(factionData.getName(), "§c" + FactionManager.getPlayerFactionRankName(player) + " " + player.getName() + " hat " + player1.getName() + " von der Blacklist gelöscht.");
                                                FactionManager.blacklistDataMap.remove(blacklistData.getId());
                                            } catch (SQLException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }
                                    if (!canDo)
                                        player.sendMessage(Main.error + player1.getName() + " ist nicht auf der Blacklist.");
                                } else {
                                    player.sendMessage(Main.error + "Dieser Befehl ist erst ab Rang 5+ verfübar.");
                                }
                            }
                        } else {
                            player.sendMessage(Main.error + "Syntax-Fehler: /blacklist add [Spieler] [Preis] [Kills] [Grund]");
                        }
                    } else {
                        player.sendMessage(Main.error + "Syntax-Fehler: /blacklist [add/remove] [Spieler] [Preis] [Kills] [Grund]");
                    }
                }
            } else {
                player.sendMessage(Main.error + "Deine Fraktion hat keine Blacklist.");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("add");
            suggestions.add("remove");

            return suggestions;
        }
        return null;
    }
}
