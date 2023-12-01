package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SelfCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    public SelfCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("self", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        playerManager.openInterActionMenu((Player) sender, (Player) sender);
        return false;
    }
}
