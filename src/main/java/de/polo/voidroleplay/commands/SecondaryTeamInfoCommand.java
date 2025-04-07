package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.Prefix;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class SecondaryTeamInfoCommand implements CommandExecutor {
    public SecondaryTeamInfoCommand() {
        Main.registerCommand("secondaryteaminfo", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /secondaryteaminfo [Team]");
            return false;
        }
        String[] teams = {"Bau-Team", "Event-Team", "PR-Team"};
        for (String team : teams) {
            if (!args[0].equalsIgnoreCase(team)) continue;
            Connection connection = Main.getInstance().coreDatabase.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM players WHERE secondaryTeam = ?");
            statement.setString(1, team);
            ResultSet result = statement.executeQuery();
            player.sendMessage("§7   ===§8[§6" + team + "§8]§7===");
            while (result.next()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(result.getString("uuid")));
                if (offlinePlayer.getName() == null) continue;
                player.sendMessage("§8 ➥ §e" + offlinePlayer.getName());
            }
        }
        return false;
    }
}
