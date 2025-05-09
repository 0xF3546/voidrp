package de.polo.core.commands;

import de.polo.api.VoidAPI;
import de.polo.api.jobs.TransportJob;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static de.polo.api.Utils.enums.Prefix.ERROR;

public class DropCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public DropCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("drop", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
        if (voidPlayer.getActiveJob() != null) {
            if (!(voidPlayer.getActiveJob() instanceof TransportJob)) {
                voidPlayer.sendMessage("Du hast nicht den passenden Job angenommen.", ERROR);
            }
            ((TransportJob) voidPlayer.getActiveJob()).handleDrop(voidPlayer);
        } else {
            voidPlayer.sendMessage("Du hast keinen Job angenommen.", ERROR);
        }
        return false;
    }
}
