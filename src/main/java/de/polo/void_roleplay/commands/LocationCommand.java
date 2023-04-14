package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.LocationManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class LocationCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args){
        Player p = (Player) sender;

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(" ").append(arg);
        }

        if(p.hasPermission("lobby.admin")){
            LocationManager.setLocation(String.valueOf(message), p);
            p.sendMessage(Main.prefix + "Du hast die Location §c" + message + " §7gesetzt");
        } else {
            p.sendMessage(Main.error_nopermission);
        }
        return false;
    }

}
