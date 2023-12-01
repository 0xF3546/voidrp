package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuflegenCommand implements CommandExecutor {
    private final Utils utils;
    public AuflegenCommand(Utils utils) {
        this.utils = utils;
        Main.registerCommand("auflegen", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        utils.phoneUtils.closeCall(player);
        return false;
    }
}
