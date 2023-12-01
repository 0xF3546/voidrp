package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JailtimeCommand implements CommandExecutor {
    private PlayerManager playerManager;
    public JailtimeCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("jailtime", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.isJailed()) {
            player.sendMessage("§8[§cGefängnis§8] §7Du bist noch §l" + playerData.getHafteinheiten() + "§7 Minuten im Gefängnis.");
        } else {
            player.sendMessage(Main.error + "Du bist nicht im Gefängnis.");
        }
        return false;
    }
}
