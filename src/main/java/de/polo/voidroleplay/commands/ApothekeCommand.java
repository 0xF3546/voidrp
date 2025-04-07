package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.faction.apotheke.Apotheke;
import de.polo.voidroleplay.location.services.impl.LocationManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.gameplay.GamePlay;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ApothekeCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    private final GamePlay gamePlay;

    public ApothekeCommand(PlayerManager playerManager, LocationManager locationManager, GamePlay gamePlay) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.gamePlay = gamePlay;
        Main.registerCommand("apotheke", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        for (Apotheke apotheke : gamePlay.apotheke.getApotheken()) {
            if (locationManager.getLocation("apotheke-" + apotheke.getId()) == null) {
                player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe einer Apotheke!");
                return false;
            }
            if (locationManager.getDistanceBetweenCoords(player, "apotheke-" + apotheke.getId()) < 5) {
                gamePlay.apotheke.openApotheke(player, apotheke.getId());
                return false;
            }
        }
        player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe einer Apotheke!");
        return false;
    }
}
