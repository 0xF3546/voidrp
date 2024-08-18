package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.GamePlay.MilitaryDrop;
import de.polo.voidroleplay.utils.Prefix;
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
public class TestCommand implements CommandExecutor {
    public TestCommand() {
        Main.registerCommand("test", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (!player.hasPermission("OP")) return false;
        if (!MilitaryDrop.ACTIVE) {
            player.sendMessage(Prefix.infoPrefix + "Du hast das Event aktiviert.");
            MilitaryDrop.ACTIVE = true;
            return false;
        }
        Main.getInstance().gamePlay.militaryDrop.start();
        player.sendMessage(Prefix.infoPrefix + "Du hast das Event gestartet.");
        return false;
    }
}
