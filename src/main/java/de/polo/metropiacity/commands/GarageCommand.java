package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.LocationManager;
import de.polo.metropiacity.Utils.Vehicles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GarageCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        int station = LocationManager.isPlayerNearGarage(player);
        if (station != 0) {
            Vehicles.openGarage(player, station, true);
        } else {
            player.sendMessage(Main.error + "Du bist bei keiner Garage.");
        }
        return false;
    }
}
