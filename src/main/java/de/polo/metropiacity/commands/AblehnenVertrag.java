package de.polo.metropiacity.commands;

import de.polo.metropiacity.utils.VertragUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AblehnenVertrag implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        VertragUtil.denyVertrag(player);
        return false;
    }
}
