package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.location.services.impl.LocationManager;
import de.polo.core.utils.Prefix;
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
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        Player p = (Player) sender;

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(" ").append(arg);
        }

        if (p.hasPermission("lobby.admin")) {
            locationManager.setLocation(String.valueOf(message), p);
            p.sendMessage(Prefix.MAIN + "Du hast die Location §c" + message + " §7gesetzt");
        } else {
            p.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }

}
