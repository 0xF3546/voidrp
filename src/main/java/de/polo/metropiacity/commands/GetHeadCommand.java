package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetHeadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (player.hasPermission("OP")) {
            if (!(args.length == 1)) {
                player.sendMessage(Main.error +"Syntax-Fehler: /gethead [Value]");
                return false;
            }
            player.getInventory().addItem(ItemManager.createCustomHead(args[0], 1, 0, "§6Kopf", null));
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
