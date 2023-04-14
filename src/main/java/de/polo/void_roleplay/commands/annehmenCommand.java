package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Utils.VertragUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class annehmenCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        try {
            VertragUtil.acceptVertrag(player);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
