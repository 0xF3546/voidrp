package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PlayerManager;
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
            player.sendMessage("§cGefängnis §8» §7Du bist noch §l" + playerData.getHafteinheiten() + "§7 Minuten im Gefängnis.");
        } else {
            player.sendMessage(Main.error + "Du bist nicht im Gefängnis.");
        }
        return false;
    }
}
