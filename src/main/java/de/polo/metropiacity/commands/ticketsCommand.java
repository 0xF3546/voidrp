package de.polo.metropiacity.commands;

import de.polo.metropiacity.Utils.PlayerManager;
import de.polo.metropiacity.Utils.SupportManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ticketsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (PlayerManager.perms(player) >= 30) {
            player.sendMessage("§6§lTicketübersicht§8:");
            for (int i = 0; i < SupportManager.playerTickets.size(); i++) {
                String creator = SupportManager.playerTickets.get(i);
                TextComponent message = new TextComponent("§8 ➥ §e" + creator);
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§e" + creator + "'s Ticket annehmen")));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptsupport " + creator));
                player.spigot().sendMessage(message);
            }
        }
        return false;
    }
}
