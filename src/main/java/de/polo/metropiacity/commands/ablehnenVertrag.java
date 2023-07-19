package de.polo.metropiacity.commands;

import de.polo.metropiacity.Utils.VertragUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class ablehnenVertrag implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        VertragUtil.denyVertrag(player);
        return false;
    }
}
