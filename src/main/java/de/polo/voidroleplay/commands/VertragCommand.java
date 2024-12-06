package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.VertragUtil;
import org.bukkit.Bukkit;
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
public class VertragCommand implements CommandExecutor {
    public VertragCommand() {
        Main.registerCommand("vertrag", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (args.length < 2) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /vertrag [Spieler] [Bedingung]");
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Prefix.ERROR + "Spieler ist nicht online.");
            return false;
        }
        if (target.getLocation().distance(player.getLocation()) > 5) {
            player.sendMessage(Prefix.ERROR + target.getName() + " ist nicht in der nähe.");
            return false;
        }
        StringBuilder reason = new StringBuilder(args[1]);
        for (int i = 2; i < args.length; i++) {
            reason.append(" ").append(args[i]);
        }
        VertragUtil.setVertrag(player, target, "vertrag", reason.toString());
        target.sendMessage("§6" + player.getName() + " hat dir einen Vertrag angeboten§8:§7 " + reason);
        Main.getInstance().utils.vertragUtil.sendInfoMessage(target);
        player.sendMessage("§6Du hst " + target.getName() + " einen Vertrag angeboten§8:§7 " + reason);
        return false;
    }
}
