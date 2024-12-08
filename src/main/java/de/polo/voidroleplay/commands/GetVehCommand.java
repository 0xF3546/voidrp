package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
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

public class GetVehCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public GetVehCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("getveh", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 60) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (!playerData.isAduty()) {
            player.sendMessage(Main.admin_error + "Du bist nicht im Admindienst!");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.admin_error + "Syntax-Fehler: /getvehicle [ID]");
            return false;
        }
        for (Entity entity : Bukkit.getWorld(player.getWorld().getName()).getEntities()) {
            if (entity.getType() == EntityType.MINECART) {
                try {
                    if (entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER) != null) {
                        int id = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER);
                        if (Integer.parseInt(args[0]) == (id)) {
                            entity.teleport(player.getLocation());
                            player.sendMessage(Prefix.ADMIN + "Du hast das Fahrzeug mit der ID §l" + args[0] + "§7 zu dir teleportiert.");
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
