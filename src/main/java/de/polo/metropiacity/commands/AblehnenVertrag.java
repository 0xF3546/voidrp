package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.Utils;
import de.polo.metropiacity.utils.VertragUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AblehnenVertrag implements CommandExecutor {
    private  final Utils utils;
    public AblehnenVertrag(Utils utils) {
        this.utils = utils;
        Main.registerCommand("ablehnen", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        utils.vertragUtil.denyVertrag(player);
        return false;
    }
}
