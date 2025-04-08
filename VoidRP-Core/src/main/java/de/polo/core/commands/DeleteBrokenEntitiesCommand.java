package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.utils.Prefix;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class DeleteBrokenEntitiesCommand implements CommandExecutor {
    public DeleteBrokenEntitiesCommand() {
        Main.registerCommand("deletebrokenentities", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (!player.hasPermission("*")) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }

        for (Entity entity : player.getWorld().getEntities()) {
            if (entity.getLocation().distance(player.getLocation()) > 10) {
                continue;
            }

            if (entity instanceof Villager) {
                String villager_name = entity.getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "name"), PersistentDataType.STRING);
                if (villager_name == null) {
                    entity.remove();
                    player.sendMessage(Prefix.GAMEDESIGN + "Ein NPC wurde gelöscht.");
                }
            } else if (entity instanceof Vehicle) {
                NamespacedKey key_id = new NamespacedKey(Main.getInstance(), "id");
                if (entity.getPersistentDataContainer().get(key_id, PersistentDataType.INTEGER) == null) {
                    entity.remove();
                    player.sendMessage(Prefix.GAMEDESIGN + "Fahrzeug wurde gelöscht.");
                }
            } else if (entity instanceof ArmorStand) {
                NamespacedKey key_id = new NamespacedKey(Main.getInstance(), "id");
                if (entity.getPersistentDataContainer().get(key_id, PersistentDataType.INTEGER) == null) {
                    entity.remove();
                    player.sendMessage(Prefix.GAMEDESIGN + "Rüstungsständer wurde gelöscht.");
                }
            } else if (entity instanceof Arrow) {
                entity.remove();
                player.sendMessage(Prefix.GAMEDESIGN + "Ein Pfeil wurde gelöscht.");
            }
        }
        return true;
    }
}