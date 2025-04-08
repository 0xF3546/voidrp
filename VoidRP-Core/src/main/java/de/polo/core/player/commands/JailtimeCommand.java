package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JailtimeCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public JailtimeCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("jailtime", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.isJailed()) {
            player.sendMessage("§8[§6Gefängnis§8] §7Du bist noch " + playerData.getHafteinheiten() + " Minuten im Gefängnis.");
        } else {
            player.sendMessage(Prefix.ERROR + "Du bist nicht im Gefängnis.");
        }
        return false;
    }
}
