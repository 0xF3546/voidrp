package de.polo.metropiacity.commands;

import de.polo.metropiacity.utils.PhoneUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuflegenCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PhoneUtils.closeCall(player);
        return false;
    }
}
