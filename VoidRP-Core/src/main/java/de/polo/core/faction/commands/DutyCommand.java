package de.polo.core.faction.commands;

import de.polo.core.Main;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.location.services.impl.LocationManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DutyCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final LocationManager locationManager;

    public DutyCommand(PlayerManager playerManager, FactionManager factionManager, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        Main.registerCommand("duty", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR + "Du bist in keiner Fraktion");
            return false;
        }
        Location location = locationManager.getLocation("duty_" + playerData.getFaction());
        if (location == null) {
            player.sendMessage(Prefix.ERROR + "Deine Fraktion hat kein Dienst-Punkt.");
            return false;
        }
        if (location.distance(player.getLocation()) > 5) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe deines Dienst-Punktes.");
            return false;
        }
        if (playerData.isDuty()) {
            factionManager.setDuty(player, false);
            factionManager.sendMessageToFaction(playerData.getFaction(), player.getName() + " hat den Dienst verlassen.");
        } else {
            Main.getInstance().beginnerpass.didQuest(player, 9);
            factionManager.setDuty(player, true);
            factionManager.sendMessageToFaction(playerData.getFaction(), player.getName() + " hat den Dienst betreten.");
        }
        return false;
    }
}
