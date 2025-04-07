package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetHeadCommand implements CommandExecutor {
    public GetHeadCommand() {
        Main.registerCommand("gethead", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (player.hasPermission("OP")) {
            if (!(args.length == 1)) {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /gethead [Value]");
                return false;
            }
            player.getInventory().addItem(ItemManager.createCustomHead(args[0], 1, 0, "§6Kopf", null));
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }
}
