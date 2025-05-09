package de.polo.voidroleplay.admin.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.manager.SupportManager;
import de.polo.voidroleplay.utils.Prefix;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SupportCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final SupportManager supportManager;

    public SupportCommand(PlayerManager playerManager, SupportManager supportManager) {
        this.playerManager = playerManager;
        this.supportManager = supportManager;
        Main.registerCommand("support", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            if (!supportManager.ticketCreated(player)) {
                StringBuilder msg = new StringBuilder(args[0]);
                for (int i = 1; i < args.length; i++) {
                    msg.append(' ').append(args[i]);
                }
                supportManager.createTicketAsync(player, String.valueOf(msg))
                        .thenAccept(ticket -> {
                            player.sendMessage(Prefix.SUPPORT + "Du hast ein Ticket §aerstellt§7. §o(TicketID: #" + ticket.getId() + ")");
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                if (playerManager.isTeam(players)) {
                                    players.sendMessage(Prefix.SUPPORT + "§a" + player.getName() + "§7 hat ein Ticket erstellt. Grund: " + msg);

                                    TextComponent annehmen = new TextComponent("§aTicket annehmen");
                                    annehmen.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a" + player.getName() + "'s Ticket annehmen")));
                                    annehmen.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptsupport " + player.getName()));

                                    TextComponent message = new TextComponent(Prefix.SUPPORT);
                                    message.addExtra(annehmen);

                                    players.spigot().sendMessage(message);
                                }
                            }
                        });

            } else {
                player.sendMessage(Prefix.SUPPORT + "Du hast bereits ein Ticket offen.");
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /support [Anliegen]");
        }
        return false;
    }
}
