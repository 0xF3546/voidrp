package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class GoToVehCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public GoToVehCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("gotoveh", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 60) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (!playerData.isAduty()) {
            player.sendMessage(Prefix.ADMIN_ERROR + "Du bist nicht im Admindienst!");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ADMIN_ERROR + "Syntax-Fehler: /gotovehicle [ID]");
            return false;
        }
        for (Entity entity : Bukkit.getWorld(player.getWorld().getName()).getEntities()) {
            if (entity.getType() == EntityType.MINECART) {
                try {
                    if (entity.getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "id"), PersistentDataType.INTEGER) != null) {
                        int id = entity.getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "id"), PersistentDataType.INTEGER);
                        if (Integer.parseInt(args[0]) == id) {
                            player.teleport(entity.getLocation());
                            player.sendMessage(Prefix.ADMIN + "Du hast dich zum Fahrzeug mit der ID §l" + args[0] + "§7 teleportiert.");
                        }
                    }
                } catch (Exception ex) {
                    continue;
                }
            }
        }
        return false;
    }
}
