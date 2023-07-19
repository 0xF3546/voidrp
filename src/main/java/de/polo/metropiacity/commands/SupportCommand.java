package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.SupportManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class SupportCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            if (!SupportManager.ticketCreated(player)) {
                player.sendMessage(Main.support_prefix + "Du hast ein Ticket §aerstellt§7.");
                StringBuilder msg = new StringBuilder(args[0]);
                for (int i = 1; i < args.length; i++) {
                    msg.append(' ').append(args[i]);
                }
                SupportManager.createTicket(player, String.valueOf(msg));
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (PlayerManager.isTeam(players)) {
                        players.sendMessage(Main.support_prefix + "§a" + player.getName() + "§7 hat ein Ticket erstellt. Grund: " + msg);

                        TextComponent annehmen = new TextComponent("§aTicket annehmen");
                        annehmen.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a" + player.getName() + "'s Ticket annehmen")));
                        annehmen.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptsupport " + player.getName()));

                        TextComponent message = new TextComponent(Main.support_prefix);
                        message.addExtra(annehmen);

                        players.spigot().sendMessage(message);
                    }
                }
                try {
                    Statement statement = MySQL.getStatement();
                    statement.execute("INSERT INTO `tickets` (`creator`, `reason`) VALUES ('" + player.getUniqueId() + "', '" + msg + "')");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                player.sendMessage(Main.support_prefix + "Du hast bereits ein Ticket offen.");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /support [Anliegen]");
        }
        return false;
    }
}
