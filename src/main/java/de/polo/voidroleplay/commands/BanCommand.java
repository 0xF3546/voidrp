package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BanCommand implements CommandExecutor, TabCompleter {

    private final PlayerManager playerManager;
    private final AdminManager adminManager;

    public BanCommand(PlayerManager playerManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        Main.registerCommand("ban", this);
        Main.addTabCompeter("ban", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return false;
        }

        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());

        if (playerData.getPermlevel() < 70) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }

        if (args.length < 4) {
            player.sendMessage(Prefix.ADMIN_ERROR + "Syntax-Fehler: /ban [name/uuid] [Wert] [Zeit] [Grund]");
            return false;
        }

        String targetType = args[0].toLowerCase();
        String targetValue = args[1];
        String banDuration = args[2].toLowerCase();
        String banReason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        LocalDateTime banEndDate = parseBanDuration(banDuration);
        if (banEndDate == null) {
            player.sendMessage(Prefix.ADMIN_ERROR + "Ungültige Zeitangabe. Beispiele: 1h, 2d, 3m");
            return false;
        }

        if ("name".equals(targetType)) {
            banPlayerByName(player, targetValue, banReason, banEndDate);
        } else if ("uuid".equals(targetType)) {
            banPlayerByUUID(player, targetValue, banReason, banEndDate);
        } else {
            player.sendMessage(Prefix.ADMIN_ERROR + "Ungültiger Typ. Verwende 'name' oder 'uuid'.");
            return false;
        }

        return true;
    }

    private void banPlayerByName(Player executor, String playerName, String reason, LocalDateTime endDate) {
        UUID targetUUID = null;

        // Check online players
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(playerName)) {
                targetUUID = onlinePlayer.getUniqueId();
                kickAndSavePlayer(onlinePlayer, reason);
                break;
            }
        }

        if (targetUUID == null) {
            try (Connection connection = Main.getInstance().mySQL.getConnection();
                 PreparedStatement stmt = connection.prepareStatement("SELECT uuid FROM players WHERE player_name = ?")) {
                stmt.setString(1, playerName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        targetUUID = UUID.fromString(rs.getString("uuid"));
                    }
                }
            } catch (SQLException e) {
                executor.sendMessage(Prefix.ADMIN_ERROR + "Datenbankfehler: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        if (targetUUID != null) {
            applyBan(targetUUID, playerName, executor.getName(), reason, endDate);
            broadcastBanMessage(executor, playerName, reason);
        } else {
            executor.sendMessage(Prefix.ADMIN_ERROR + "Spieler nicht gefunden.");
        }
    }

    private void banPlayerByUUID(Player executor, String uuidString, String reason, LocalDateTime endDate) {
        UUID targetUUID;
        try {
            targetUUID = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            executor.sendMessage(Prefix.ADMIN_ERROR + "Ungültige UUID.");
            return;
        }

        String targetName = null;
        try (Connection connection = Main.getInstance().mySQL.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT player_name FROM players WHERE uuid = ?")) {
            stmt.setString(1, uuidString);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    targetName = rs.getString("player_name");
                }
            }
        } catch (SQLException e) {
            executor.sendMessage(Prefix.ADMIN_ERROR + "Datenbankfehler: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (targetName != null) {
            applyBan(targetUUID, targetName, executor.getName(), reason, endDate);
            broadcastBanMessage(executor, targetName, reason);
        } else {
            executor.sendMessage(Prefix.ADMIN_ERROR + "Spieler nicht gefunden.");
        }
    }

    private void kickAndSavePlayer(Player player, String reason) {
        try {
            playerManager.savePlayer(player);
            player.closeInventory();
            player.kickPlayer("§8• §6§lVoidRoleplay §8•\n\n§cDu wurdest vom Server gebannt.\nGrund§8:§7 " + reason);
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Speichern des Spielers", e);
        }
    }

    private void applyBan(UUID uuid, String name, String punisher, String reason, LocalDateTime endDate) {
        try (Connection connection = Main.getInstance().mySQL.getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM player_bans WHERE uuid = ?");
             PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO player_bans (uuid, name, reason, punisher, date) VALUES (?, ?, ?, ?, ?)")) {

            deleteStmt.setString(1, uuid.toString());
            deleteStmt.executeUpdate();

            insertStmt.setString(1, uuid.toString());
            insertStmt.setString(2, name);
            insertStmt.setString(3, reason);
            insertStmt.setString(4, punisher);
            insertStmt.setObject(5, endDate);
            insertStmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Aktualisieren der Bann-Daten", e);
        }
    }

    private void broadcastBanMessage(Player punisher, String targetName, String reason) {
        Bukkit.broadcastMessage(ChatColor.RED + punisher.getName() + " hat " + targetName + " gebannt. Grund: " + reason);
        adminManager.insertNote("System", targetName, "Spieler wurde gebannt (" + reason + ")");
    }

    private LocalDateTime parseBanDuration(String duration) {
        try {
            LocalDateTime now = LocalDateTime.now();
            if (duration.endsWith("h")) {
                return now.plusHours(Integer.parseInt(duration.replace("h", "")));
            } else if (duration.endsWith("d")) {
                return now.plusDays(Integer.parseInt(duration.replace("d", "")));
            } else if (duration.endsWith("m")) {
                return now.plusMonths(Integer.parseInt(duration.replace("m", "")));
            } else if (duration.endsWith("s")) {
                return now.plusSeconds(Integer.parseInt(duration.replace("s", "")));
            } else if (duration.endsWith("y")) {
                return now.plusYears(Integer.parseInt(duration.replace("y", "")));
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("name", "uuid");
        }
        if (args.length == 3) {
            return Collections.singletonList("[<Zeit>]");
        }
        if (args.length == 4) {
            return Collections.singletonList("[<Grund>]");
        }
        return null;
    }
}