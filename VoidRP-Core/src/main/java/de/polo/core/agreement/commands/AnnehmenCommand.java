package de.polo.core.agreement.commands;

import de.polo.core.Main;
import de.polo.api.VoidAPI;
import de.polo.core.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static de.polo.core.Main.agreementService;

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
