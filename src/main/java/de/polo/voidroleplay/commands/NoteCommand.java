package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Utils;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class NoteCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;
    public NoteCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("note", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 60) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /note [Spieler] [<Eintrag>]");
            return false;
        }
        if (args.length == 1) {
            OfflinePlayer offlinePlayer = utils.getOfflinePlayer(args[0]);
            if (offlinePlayer == null) {
                player.sendMessage(Main.error + "Der Spieler konnte nicht gefunden werden.");
                return false;
            }
            try {
                Statement statement = Main.getInstance().mySQL.getStatement();
                ResultSet res = statement.executeQuery("SELECT *, DATE_FORMAT(entryAdded, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM notes WHERE target = '" + offlinePlayer.getUniqueId() + "'");
                if (!res.next()) {
                    player.sendMessage("§8 » §eKeine Einträge vorhanden.");
                    return false;
                }
                player.sendMessage("§7   ===§8[§e" + offlinePlayer.getName() + "'s Notes§8]§7===");
                TextComponent db = new TextComponent("§8 » §e" + res.getInt(1) + "§8 × §e" + res.getString(4));
                String punisher = null;
                if (res.getString(2).equalsIgnoreCase("System")) {
                    punisher = "System";
                } else {
                    OfflinePlayer punisher2 = Bukkit.getOfflinePlayer(UUID.fromString(res.getString(2)));
                    punisher = punisher2.getName();
                }
                db.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eVergeben am " + res.getString("formatted_timestamp") + "\n§edurch: " + punisher)));
                player.spigot().sendMessage(db);
                while (res.next()) {
                    TextComponent db2 = new TextComponent("§8 » §e" + res.getInt(1) + "§8 × §e" + res.getString(4));
                    db2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§eVergeben am " + res.getString("formatted_timestamp") + "\n§edurch: " + punisher)));
                    player.spigot().sendMessage(db2);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
        OfflinePlayer offlinePlayer = utils.getOfflinePlayer(args[0]);
        if (offlinePlayer == null) {
            player.sendMessage(Main.error + "Der Spieler konnte nicht gefunden werden.");
            return false;
        }
        try {
            Statement statement = Main.getInstance().mySQL.getStatement();
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
