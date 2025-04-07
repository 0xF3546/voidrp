package de.polo.voidroleplay.faction.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.location.services.impl.LocationManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static de.polo.voidroleplay.Main.navigationService;

public class FindLaboratoryCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;
    private final LocationManager locationManager;

    public FindLaboratoryCommand(PlayerManager playerManager, Utils utils, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.utils = utils;
        this.locationManager = locationManager;
        Main.registerCommand("findlaboratory", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        Location location = locationManager.getLocation(playerData.getFaction() + "_laboratory");
        if (location == null) {
            player.sendMessage(Prefix.ERROR + "Deine Fraktion hat kein Labor.");
            return false;
        }
        navigationService.createNaviByCord(player, (int) location.getX(), (int) location.getY(), (int) location.getZ());
        player.sendMessage("§aDein Labor wurde markiert.");
        return false;
    }
}
