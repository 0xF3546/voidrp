package de.polo.core.base.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.game.faction.apotheke.Apotheke;
import de.polo.core.location.services.LocationService;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.gameplay.GamePlay;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ApothekeCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final GamePlay gamePlay;

    public ApothekeCommand(PlayerManager playerManager, GamePlay gamePlay) {
        this.playerManager = playerManager;
        this.gamePlay = gamePlay;
        Main.registerCommand("apotheke", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        LocationService locationService = VoidAPI.getService(LocationService.class);
        for (Apotheke apotheke : gamePlay.apotheke.getApotheken()) {
            if (locationService.getLocation("apotheke-" + apotheke.getId()) == null) {
                player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe einer Apotheke!");
                return false;
            }
            if (locationService.getDistanceBetweenCoords(player, "apotheke-" + apotheke.getId()) < 5) {
                gamePlay.apotheke.openApotheke(player, apotheke.getId());
                return false;
            }
        }
        player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe einer Apotheke!");
        return false;
    }
}
