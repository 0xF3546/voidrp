package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.TabCompletion;
import de.polo.voidroleplay.storage.BlacklistData;
import de.polo.voidroleplay.faction.entity.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (!factionData.hasBlacklist() && !args[0].equalsIgnoreCase("pay")) {
            player.sendMessage(Prefix.ERROR + "Deine Fraktion hat keine Blacklist.");
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
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /blacklist [add/remove/pay/all] [Spieler/Fraktion] [<Kills>] [<Preis>] [<Grund>]");
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
        } else if (args[0].equalsIgnoreCase("add")) {
            if (args.length >= 4) {
                Player player1 = Bukkit.getPlayer(args[1]);
                if (player1 == null) {
                    player.sendMessage(Prefix.ERROR + args[0] + " ist nicht online.");
                    return false;
                }
                if (playerData.getFactionGrade() < 3) {
                    player.sendMessage(Prefix.ERROR + "Dieser Befehl ist erst ab Rang 3+ verfübar.");
                    return false;
                }
                if (player1.getName().equals(player.getName())) {
                    player.sendMessage(Prefix.ERROR + "Du kannst dich selbst nicht auf die Blacklist setzen.");
                    return false;
                }
                PlayerData targetPlayerData = playerManager.getPlayerData(player1);
                if (targetPlayerData.getFaction() != null) {
                    if (targetPlayerData.getFaction().equalsIgnoreCase(playerData.getFaction())) {
                        player.sendMessage(Prefix.ERROR + "Fraktionsmitglieder können nicht auf die Blacklist.");
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
                    player.sendMessage(Prefix.ERROR + player1.getName() + " ist bereits auf der Blacklist.");
                    return false;
                }
                int price = Integer.parseInt(args[3]);
                int kills = Integer.parseInt(args[2]);
                String reason = "";
                for (int i = 4; i < args.length; i++) {
                    reason = reason + " " + args[i];
                }
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyy '|' HH:mm:ss ");
                String newDate = formatter.format(new Date());
                String finalReason = reason;
                Main.getInstance().getCoreDatabase().insertAndGetKeyAsync("INSERT INTO blacklist (uuid, faction, kills, price, date, reason) VALUES (?, ?, ?, ?, ?, ?)",
                        player1.getUniqueId().toString(),
                        factionData.getName(),
                        kills,
                        price,
                        newDate,
                        reason
                ).thenApply(key -> {
                    if (key.isPresent()) {
                        BlacklistData blacklistData = new BlacklistData();
                        blacklistData.setDate(newDate);
                        blacklistData.setPrice(price);
                        blacklistData.setReason(finalReason);
                        blacklistData.setKills(kills);
                        blacklistData.setFaction(factionData.getName());
                        blacklistData.setId(key.get());
                        blacklistData.setUuid(player1.getUniqueId().toString());
                        factionManager.addBlacklist(key.get(), blacklistData);
                        factionManager.sendMessageToFaction(factionData.getName(), factionManager.getPlayerFactionRankName(player) + " " + player.getName() + " hat " + player1.getName() + " auf die Blacklist gesetzt.");
                        player1.sendMessage("§8[§cBlacklist§8]§c Du wurdest auf die Blacklist von " + factionData.getFullname() + " gesetzt.");
                        player1.sendMessage("§8[§cBlacklist§8]§c " + kills + " Kills §8| §c" + price + "$§8 | §c" + finalReason);
                    }
                    return null;
                });
            }
        } else if (args[0].equalsIgnoreCase("remove")) {
            Player player1 = Bukkit.getPlayer(args[1]);
            if (!(args.length <= 2)) {
                return false;
            }
            if (player1 == null) {
                player.sendMessage(Prefix.ERROR + args[0] + " ist nicht online.");
                return false;
            }
            if (playerData.getFactionGrade() < 4) {
                player.sendMessage(Prefix.ERROR + "Dieser Befehl ist erst ab Rang 4+ verfübar.");
                return false;
            }
            boolean canDo = false;
            for (BlacklistData blacklistData : factionManager.getBlacklists()) {
                if (blacklistData.getUuid().equals(player1.getUniqueId().toString()) && blacklistData.getFaction().equals(factionData.getName())) {
                    canDo = true;
                    Main.getInstance().getCoreDatabase().deleteAsync("DELETE FROM blacklist WHERE id = ?", blacklistData.getId());
                    factionManager.sendMessageToFaction(factionData.getName(), "§c" + factionManager.getPlayerFactionRankName(player) + " " + player.getName() + " hat " + player1.getName() + " von der Blacklist gelöscht.");
                    factionManager.removeBlacklist(blacklistData.getId());
                    player1.sendMessage("§8[§cBlacklist§8]§7 " + player.getName() + " hat dich von der Blacklist der " + factionData.getFullname() + " gelöscht");
                    return false;
                }
            }
            player.sendMessage(Prefix.ERROR + player1.getName() + " ist nicht auf der Blacklist.");
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
                                        Statement statement = Main.getInstance().coreDatabase.getStatement();
                                        statement.execute("DELETE FROM blacklist WHERE id = " + blacklistData.getId());
                                        factionManager.removeBlacklist(blacklistData.getId());
                                        return false;
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei.");
                                    return false;
                                }
                            }
                        }
                    }
                    player.sendMessage(Prefix.ERROR + "Du bist nicht auf dieser Blacklist.");
                    return false;
                }
            }
            player.sendMessage(Prefix.ERROR + "Die Fraktion konnte nicht gefunden werden.");
        } else {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /blacklist add [Spieler] [Kills] [Preis] [Grund]");
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, List.of("all", "add", "remove", "pay"))
                .addAtIndex(2, Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .toList())
                .build();
    }
}
