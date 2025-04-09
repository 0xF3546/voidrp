package de.polo.core.admin.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.admin.services.impl.AdminManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static de.polo.core.Main.adminService;

public class ResetBonusEntryCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;

    public ResetBonusEntryCommand(PlayerManager playerManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;

        Main.registerCommand("resetbonusentry", this);
        Main.addTabCompleter("resetbonusentry", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 90) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            for (PlayerData p : playerManager.getPlayers()) {
                p.setReceivedBonus(false);
            }
            Connection connection = Main.getInstance().coreDatabase.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE players SET bonusReceived = false");
            statement.execute();
            statement.close();
            connection.close();
            adminService.send_message(player.getName() + " hat die Bonis zurückgesetzt.", Color.ORANGE);
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Prefix.ERROR + "Der Spieler ist nicht online.");
            return false;
        }
        PlayerData targetData = playerManager.getPlayerData(targetplayer);
        targetData.setReceivedBonus(false);
        Connection connection = Main.getInstance().coreDatabase.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE players SET bonusReceived = false WHERE uuid = ?");
        statement.setString(1, targetplayer.getUniqueId().toString());
        statement.execute();
        statement.close();
        connection.close();
        adminService.send_message(player.getName() + " hat die " + targetplayer.getName() + "'s Bonus zurückgesetzt.", Color.ORANGE);
        targetplayer.sendMessage("§8[§cAdmin§8]§7 " + playerData.getRang() + " " + player.getName() + " hat deinen Bonus zurückgesetzt.");
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> value = new ObjectArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            value.add(player.getName());
        }
        return value;
    }
}
