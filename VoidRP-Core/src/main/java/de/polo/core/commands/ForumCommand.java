package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForumCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;

    public ForumCommand(PlayerManager playerManager, FactionManager factionManager, Utils utils) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        Main.registerCommand("forum", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /forum [link/unlink]");
            return false;
        }
        return false;
    }
}
