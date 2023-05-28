package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class getvehCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getPermlevel() >= 60) {
            if (playerData.isAduty()) {
                if (args.length >= 1) {
                    for (Entity entity : Bukkit.getWorld(player.getWorld().getName()).getEntities()) {
                        if (entity.getType() == EntityType.MINECART) {
                            if (Integer.parseInt(args[0]) == (entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER))) {
                                entity.teleport(player.getLocation());
                                player.sendMessage(Main.admin_prefix + "Du hast das Fahrzeug mit der ID §l" + args[0] + "§7 zu dir teleportiert.");
                            }
                        }
                    }
            } else {
                player.sendMessage(Main.admin_error + "Syntax-Fehler: /getvehicle [ID]");
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
