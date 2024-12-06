package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.TeamSpeak;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TSLinkCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public TSLinkCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("tslink", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /tslink [Eindeutige ID]");
            return false;
        }
        player.sendMessage("§8[§3TeamSpeak§8]§b Dir wurde eine Nachricht im TS3 geschickt!");
        TeamSpeak.verifyUser(player, args[0]);
        return false;
    }
}
