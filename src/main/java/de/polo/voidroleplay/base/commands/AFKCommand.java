package de.polo.voidroleplay.base.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.player.PlayerPacket;
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
public class AFKCommand implements CommandExecutor {
    private final Utils utils;

    public AFKCommand(Utils utils) {
        this.utils = utils;
        Main.registerCommand("afk", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        utils.setAFK(player, true);
        PlayerPacket packet = new PlayerPacket(player);
        packet.renewPacket();
        return false;
    }
}
