package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.LocationManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LocationCommand implements CommandExecutor {
    private final LocationManager locationManager;
    public LocationCommand(LocationManager locationManager) {
        this.locationManager = locationManager;
        Main.registerCommand("setloc", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args){
        Player p = (Player) sender;

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(" ").append(arg);
        }

        if(p.hasPermission("lobby.admin")){
            locationManager.setLocation(String.valueOf(message), p);
            p.sendMessage(Main.prefix + "Du hast die Location §c" + message + " §7gesetzt");
        } else {
            p.sendMessage(Main.error_nopermission);
        }
        return false;
    }

}
