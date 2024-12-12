package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.base.vehicle.Vehicles;
import de.polo.voidroleplay.manager.LocationManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GarageCommand implements CommandExecutor {
    private final LocationManager locationManager;
    private final Vehicles vehicles;

    public GarageCommand(LocationManager locationManager, Vehicles vehicles) {
        this.locationManager = locationManager;
        this.vehicles = vehicles;
        Main.registerCommand("garage", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        int station = locationManager.isPlayerNearGarage(player);
        if (station != 0) {
            vehicles.openGarage(player, station, true);
        } else {
            player.sendMessage(Prefix.ERROR + "Du bist bei keiner Garage.");
        }
        return false;
    }
}
