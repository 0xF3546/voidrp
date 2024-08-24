package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.BlacklistData;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

public class BlacklistCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public BlacklistCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("blacklist", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getFaction() == null) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (!factionData.hasBlacklist() && !args[0].equalsIgnoreCase("pay")) {
            player.sendMessage(Main.error + "Deine Fraktion hat keine Blacklist.");
            return false;
        }
        if (args.length == 0) {
            player.sendMessage("§7   ===§8[§cBlacklist§8]§7===");
            player.sendMessage("§8 ➥ §" + factionData.getPrimaryColor() + factionData.getFullname());
            player.sendMessage(" ");
            for (BlacklistData blacklistData : factionManager.getBlacklists()) {
                if (blacklistData.getFaction().equals(factionData.getName())) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(blacklistData.getUuid()));
                    if (!offlinePlayer.isOnline()) continue;
                    player.sendMessage("§8 ➥ §e" + offlinePlayer.getName() + "§8 | §e" + blacklistData.getPrice() + "$ §8 | §e" + blacklistData.getKills() + " Tode §8| §e" + blacklistData.getReason() + " §8|§e " + blacklistData.getDate());
                }
            }
            return false;
        }
        if (!(args.length >= 2)) {
            player.sendMessage(Main.error + "Syntax-Fehler: /blacklist [add/remove/pay/all] [Spieler/Fraktion] [<Kills>] [<Preis>] [<Grund>]");
            return false;
        }
        if (args[0].equalsIgnoreCase("all")) {
            player.sendMessage("§7   ===§8[§cBlacklist§8]§7===");
            player.sendMessage("§8 ➥ §" + factionData.getPrimaryColor() + factionData.getFullname());
            player.sendMessage(" ");
            for (BlacklistData blacklistData : factionManager.getBlacklists()) {
                if (blacklistData.getFaction().equals(factionData.getName())) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(blacklistData.getUuid()));
                    player.sendMessage("§8 ➥ §e" + offlinePlayer.getName() + "§8 | §e" + blacklistData.getPrice() + "$ §8 | §e" + blacklistData.getKills() + " Tode §8| §e" + blacklistData.getReason() + " §8|§e " + blacklistData.getDate());
                }
            }
            return false;
        }
        else if (args[0].equalsIgnoreCase("add")) {
            if (args.length >= 4) {
                Player player1 = Bukkit.getPlayer(args[1]);
                if (player1 == null) {
                    player.sendMessage(Main.error + args[0] + " ist nicht online.");
                    return false;
                }
                if (playerData.getFactionGrade() < 3) {
                    player.sendMessage(Main.error + "Dieser Befehl ist erst ab Rang 3+ verfübar.");
                    return false;
                }
                if (player1.getName().equals(player.getName())) {
                    player.sendMessage(Main.error + "Du kannst dich selbst nicht auf die Blacklist setzen.");
                    return false;
                }
                PlayerData targetPlayerData = playerManager.getPlayerData(player1);
                if (targetPlayerData.getFaction() != null) {
                    if (targetPlayerData.getFaction().equalsIgnoreCase(playerData.getFaction())) {
                        player.sendMessage(Main.error + "Fraktionsmitglieder können nicht auf die Blacklist.");
                        return false;
                    }
                }
                boolean canDo = true;
                for (BlacklistData blacklistData : factionManager.getBlacklists()) {
                    if (blacklistData.getFaction().equals(playerData.getFaction())) {
                        if (Objects.equals(blacklistData.getUuid(), player1.getUniqueId().toString())) {
                            canDo = false;
                            break;
                        }
                    }
                }
                if (!canDo) {
                    player.sendMessage(Main.error + player1.getName() + " ist bereits auf der Blacklist.");
                    return false;
                }
                int price = Integer.parseInt(args[3]);
                int kills = Integer.parseInt(args[2]);
                String reason = "";
                for (int i = 4; i < args.length; i++) {
                    reason = reason + " " + args[i];
                }
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyy '|' HH:mm:ss ");
                    String newDate = formatter.format(new Date());
                    Statement statement = Main.getInstance().mySQL.getStatement();
                    statement.execute("INSERT INTO `blacklist` (`uuid`, `faction`, `kills`, `price`, `date`, `reason`) VALUES ('" + player1.getUniqueId() + "', '" + factionData.getName() + "', " + kills + ", " + price + ", '" + newDate + "', '" + reason + "')");
                    ResultSet checkId = statement.executeQuery("SELECT `id` FROM `blacklist` WHERE `uuid` = '" + player1.getUniqueId() + "' AND `date` = '" + newDate + "'");
                    if (checkId.next()) {
                        BlacklistData blacklistData = new BlacklistData();
                        blacklistData.setDate(newDate);
                        blacklistData.setPrice(price);
                        blacklistData.setReason(reason);
                        blacklistData.setKills(kills);
                        blacklistData.setFaction(factionData.getName());
                        blacklistData.setId(checkId.getInt(1));
                        blacklistData.setUuid(player1.getUniqueId().toString());
                        factionManager.addBlacklist(checkId.getInt(1), blacklistData);
                    }
                    factionManager.sendMessageToFaction(factionData.getName(), factionManager.getPlayerFactionRankName(player) + " " + player.getName() + " hat " + player1.getName() + " auf die Blacklist gesetzt.");
                    player1.sendMessage("§8[§cBlacklist§8]§c Du wurdest auf die Blacklist von " + factionData.getFullname() + " gesetzt.");
                    player1.sendMessage("§8[§cBlacklist§8]§c " + kills + " Kills §8| §c" + price + "$§8 | §c" + reason);
                } catch (SQLException e) {
                    player.sendMessage(Main.error + "Bitte versuche es später erneut.");
                    throw new RuntimeException(e);
                }
            }
        } else if (args[0].equalsIgnoreCase("remove")) {
            Player player1 = Bukkit.getPlayer(args[1]);
            if (!(args.length <= 2)) {
                return false;
            }
            if (player1 == null) {
                player.sendMessage(Main.error + args[0] + " ist nicht online.");
                return false;
            }
            if (playerData.getFactionGrade() < 5) {
                player.sendMessage(Main.error + "Dieser Befehl ist erst ab Rang 5+ verfübar.");
                return false;
            }
            boolean canDo = false;
            for (BlacklistData blacklistData : factionManager.getBlacklists()) {
                if (blacklistData.getUuid().equals(player1.getUniqueId().toString()) && blacklistData.getFaction().equals(factionData.getName())) {
                    canDo = true;

                    try {
                        Statement statement = Main.getInstance().mySQL.getStatement();
                        statement.execute("DELETE FROM `blacklist` WHERE `id` = " + blacklistData.getId());
                        factionManager.sendMessageToFaction(factionData.getName(), "§c" + factionManager.getPlayerFactionRankName(player) + " " + player.getName() + " hat " + player1.getName() + " von der Blacklist gelöscht.");
                        factionManager.removeBlacklist(blacklistData.getId());
                        player1.sendMessage("§8[§cBlacklist§8]§7 " + player.getName() + " hat dich von der Blacklist der " + factionData.getFullname() + " gelöscht");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return false;
                }
            }
            player.sendMessage(Main.error + player1.getName() + " ist nicht auf der Blacklist.");
        } else if (args[0].equalsIgnoreCase("pay")) {
            for (FactionData factionData1 : factionManager.getFactions()) {
                if (factionData1.getName().equalsIgnoreCase(args[1]) || factionData1.getFullname().equalsIgnoreCase(args[1])) {
                    for (BlacklistData blacklistData : factionManager.getBlacklists()) {
                        if (blacklistData.getFaction().equals(factionData1.getName())) {
                            if (blacklistData.getUuid().equalsIgnoreCase(player.getUniqueId().toString())) {
                                if (playerData.getBargeld() >= blacklistData.getPrice()) {
                                    try {
                                        playerManager.removeMoney(player, blacklistData.getPrice(), "Blacklist bezahlt - " + factionData1.getName());
                                        player.sendMessage("§8[§cBlacklist§8]§7 Du hast dich von der Blacklist von §" + factionData1.getPrimaryColor() + factionData1.getFullname() + "§7 freigekauft. §c-" + blacklistData.getPrice());
                                        for (PlayerData playerData1 : playerManager.getPlayers()) {
                                            if (playerData1.getFaction() != null) {
                                                if (playerData1.getFaction().equals(factionData1.getName())) {
                                                    Player player1 = Bukkit.getPlayer(UUID.fromString(playerData1.getUuid().toString()));
                                                    if (player1 != null) {
                                                        player1.sendMessage("§8[§cBlacklist§8] §" + factionData1.getPrimaryColor() + player.getName() + " hat sich freigekauft (§a" + blacklistData.getPrice() + "$§" + factionData1.getPrimaryColor() + ").");
                                                    }
                                                    }
                                            }
                                        }
                                        factionManager.addFactionMoney(factionData1.getName(), blacklistData.getPrice(), "Blacklist-Zahlung " + player.getName());
                                        Statement statement = Main.getInstance().mySQL.getStatement();
                                        statement.execute("DELETE FROM blacklist WHERE id = " + blacklistData.getId());
                                        factionManager.removeBlacklist(blacklistData.getId());
                                        return false;
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    player.sendMessage(Main.error + "Du hast nicht genug Geld dabei.");
                                    return false;
                                }
                            }
                        }
                    }
                    player.sendMessage(Main.error + "Du bist nicht auf dieser Blacklist.");
                    return false;
                }
            }
            player.sendMessage(Main.error + "Die Fraktion konnte nicht gefunden werden.");
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /blacklist add [Spieler] [Kills] [Preis] [Grund]");
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("all");
            suggestions.add("add");
            suggestions.add("remove");
            suggestions.add("pay");

            return suggestions;
        }
        return null;
    }
}
