package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.player.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MeCommand implements CommandExecutor {
    public MeCommand() {
        Main.registerCommand("me", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        StringBuilder message = new StringBuilder(player.getName());
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /me [Aktion]");
            return false;
        }
        for (String arg : args) {
            message.append(" ").append(arg);
        }
        ChatUtils.sendMeMessageAtPlayer(player, message.toString());
        return false;
    }
}
