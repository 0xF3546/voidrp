package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.player.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class WüfelnCommand implements CommandExecutor {
    public WüfelnCommand() {
        Main.registerCommand("würfeln", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " würfelt " + Utils.random(0, 300));
        return false;
    }
}
