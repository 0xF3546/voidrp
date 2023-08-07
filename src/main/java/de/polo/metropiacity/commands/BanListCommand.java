package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BanListCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 70) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        String query = null;
        player.sendMessage("§7   ===§8[§cBanlist§8]§7===");
        if (args.length == 0) {
            query = "SELECT *, DATE_FORMAT(date, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM player_bans";
        }
        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("search")) {
                query = "SELECT *, DATE_FORMAT(date, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM player_bans WHERE name LIKE '%" + args[1] + "%'";
            }
        }
        try {
            Statement statement = MySQL.getStatement();
            ResultSet res = statement.executeQuery(query);
            if (!res.next()) {
                player.sendMessage("§8 » §eDie Liste ist leer.");
            }
            while (res.next()) {
                TextComponent db = new TextComponent("§8 » §e" + res.getString(3) + "§8 | §e" + res.getString(4));
                db.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eBan läuft ab: " + res.getString("formatted_timestamp") + "\n§eGebannt durch: " + res.getString(5))));
                player.spigot().sendMessage(db);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
