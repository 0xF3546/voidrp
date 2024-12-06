package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.Ticket;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.SupportManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TicketsCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final SupportManager supportManager;
    public TicketsCommand(PlayerManager playerManager, SupportManager supportManager) {
        this.playerManager = playerManager;
        this.supportManager = supportManager;
        Main.registerCommand("tickets", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (playerManager.perms(player) >= 40) {
            player.sendMessage("§6§lTicketübersicht§8:");
            for (Ticket ticket : supportManager.getTickets()) {
                Player targetPlayer = Bukkit.getPlayer(ticket.getCreator());
                if (targetPlayer == null) continue;
                TextComponent message = new TextComponent("§8 ➥ §e" + targetPlayer.getName() + " | " + ticket.getReason() + " | Bearbeiter: " + ticket.getEditors().size());
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§e" + targetPlayer.getName() + "'s Ticket annehmen")));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptsupport " + targetPlayer.getName()));
                player.spigot().sendMessage(message);
            }
        }
        return false;
    }
}
