package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class speedCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() >= 70) {
            if (playerData.isAduty()) {
                if (args.length > 0) {
                    player.sendMessage(Main.admin_prefix + "Dein Fly-Speed wurde auf §c" + args[0] + "§7 gestellt.");
                    player.setFlySpeed(Float.parseFloat(args[0].replace(",", ".")));
                } else {
                    player.setFlySpeed(0.1F);
                    player.sendMessage(Main.admin_prefix + "Dein Fly-Speed wurde §czurückgesetzt§7.");
                }
            } else {
                player.sendMessage(Main.admin_error + "Du bist nicht im Admindienst!");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
