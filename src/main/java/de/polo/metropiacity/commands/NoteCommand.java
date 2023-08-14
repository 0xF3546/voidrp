package de.polo.metropiacity.commands;

import com.google.common.util.concurrent.UncheckedTimeoutException;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.DBPlayerData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.ServerManager;
import de.polo.metropiacity.utils.Utils;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.swing.plaf.nimbus.State;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class NoteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 60) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /note [Spieler] [<Eintrag>]");
            return false;
        }
        if (args.length == 1) {
            OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
            if (offlinePlayer == null) {
                player.sendMessage(Main.error + "Der Spieler konnte nicht gefunden werden.");
                return false;
            }
            try {
                Statement statement = MySQL.getStatement();
                ResultSet res = statement.executeQuery("SELECT *, DATE_FORMAT(entryAdded, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM notes WHERE target = '" + offlinePlayer.getUniqueId() + "'");
                if (!res.next()) {
                    player.sendMessage("§8 » §eKeine Einträge vorhanden.");
                    return false;
                }
                player.sendMessage("§7   ===§8[§e" + offlinePlayer.getName() + "'s Notes§8]§7===");
                TextComponent db = new TextComponent("§8 » §e" + res.getInt(1) + "§8 × §e" + res.getString(4));
                OfflinePlayer punisher = Bukkit.getOfflinePlayer(UUID.fromString(res.getString(2)));
                db.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eVergeben am " + res.getString("formatted_timestamp") + "\n§edurch: " + punisher.getName())));
                player.spigot().sendMessage(db);
                while (res.next()) {
                    TextComponent db2 = new TextComponent("§8 » §e" + res.getInt(1) + "§8 × §e" + res.getString(4));
                    db2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eVergeben am " + res.getString("formatted_timestamp") + "\n§edurch: " + punisher.getName())));
                    player.spigot().sendMessage(db2);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
        OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
        if (offlinePlayer == null) {
            player.sendMessage(Main.error + "Der Spieler konnte nicht gefunden werden.");
            return false;
        }
        try {
            Statement statement = MySQL.getStatement();
            StringBuilder msg = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; i++) {
                msg.append(" ").append(args[i]);
            }
            statement.execute("INSERT INTO notes (uuid, target, note) VALUES ('" + player.getUniqueId() + "', '" + offlinePlayer.getUniqueId() + "', '" + msg + "')");
            player.sendMessage("§8[§eNote§8]§a Eintrag für Spieler \"" + offlinePlayer.getName() + "\" hinzugefügt.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
