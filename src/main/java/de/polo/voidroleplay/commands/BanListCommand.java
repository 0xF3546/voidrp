package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
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
    private final PlayerManager playerManager;

    public BanListCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("banlist", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 70) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
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
            Statement statement = Main.getInstance().mySQL.getStatement();
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
