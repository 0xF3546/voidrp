package de.polo.voidroleplay.agreement.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.VoidAPI;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

import static de.polo.voidroleplay.Main.agreementService;

public class AnnehmenCommand implements CommandExecutor {
    private final Utils utils;

    public AnnehmenCommand(Utils utils) {
        this.utils = utils;
        Main.registerCommand("annehmen", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        agreementService.acceptVertrag(VoidAPI.getPlayer(player));
        return false;
    }
}
