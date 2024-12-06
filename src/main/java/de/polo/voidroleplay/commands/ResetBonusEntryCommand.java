package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.PlayerManager;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class ResetBonusEntryCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;

    public ResetBonusEntryCommand(PlayerManager playerManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;

        Main.registerCommand("resetbonusentry", this);
        Main.addTabCompeter("resetbonusentry", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 90) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            for (PlayerData p : playerManager.getPlayers()) {
                p.setReceivedBonus(false);
            }
            Connection connection = Main.getInstance().mySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE players SET bonusReceived = false");
            statement.execute();
            statement.close();
            connection.close();
            adminManager.send_message(player.getName() + " hat die Bonis zurückgesetzt.", ChatColor.GOLD);
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Main.error + "Der Spieler ist nicht online.");
            return false;
        }
        PlayerData targetData = playerManager.getPlayerData(targetplayer);
        targetData.setReceivedBonus(false);
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE players SET bonusReceived = false WHERE uuid = ?");
        statement.setString(1, targetplayer.getUniqueId().toString());
        statement.execute();
        statement.close();
        connection.close();
        adminManager.send_message(player.getName() + " hat die " + targetplayer.getName() + "'s Bonus zurückgesetzt.", ChatColor.GOLD);
        targetplayer.sendMessage("§8[§cAdmin§8]§7 " + playerData.getRang() + " " + player.getName() + " hat deinen Bonus zurückgesetzt.");
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> value = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            value.add(player.getName());
        }
        return value;
    }
}
