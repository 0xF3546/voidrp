package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.dataStorage.ServiceData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.StaatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelServiceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        ServiceData serviceData = StaatUtil.serviceDataMap.get(player.getUniqueId().toString());
        if (serviceData != null) {
            StaatUtil.cancelservice(player);
        } else {
            player.sendMessage(Main.error + "Du hast keinen Service offen.");
        }
        return false;
    }
}
