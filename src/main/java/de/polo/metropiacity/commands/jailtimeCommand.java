package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class jailtimeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.isJailed()) {
            player.sendMessage("§8[§cGefängnis§8] §7Du bist noch §l" + playerData.getHafteinheiten() + "§7 Minuten im Gefängnis.");
        } else {
            player.sendMessage(Main.error + "Du bist nicht im Gefängnis.");
        }
        return false;
    }
}
