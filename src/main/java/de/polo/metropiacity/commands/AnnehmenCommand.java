package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.Utils;
import de.polo.metropiacity.utils.VertragUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class AnnehmenCommand implements CommandExecutor {
    private final Utils utils;
    public AnnehmenCommand(Utils utils) {
        this.utils = utils;
        Main.registerCommand("annehmen", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        try {
            utils.vertragUtil.acceptVertrag(player);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
