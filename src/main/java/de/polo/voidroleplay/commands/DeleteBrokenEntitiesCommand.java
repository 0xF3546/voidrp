package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import org.bukkit.Bukkit;
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
            player.sendMessage(Main.error_nopermission);
            return false;
        }

        for (Entity entity : player.getWorld().getEntities()) {
            if (entity.getLocation().distance(player.getLocation()) > 10) {
                continue;
            }

            if (entity instanceof Villager) {
                String villager_name = entity.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "name"), PersistentDataType.STRING);
                if (villager_name == null) {
                    entity.remove();
                    player.sendMessage(Main.gamedesign_prefix + "Ein NPC wurde gelöscht.");
                }
            } else if (entity instanceof Vehicle) {
                NamespacedKey key_id = new NamespacedKey(Main.plugin, "id");
                if (entity.getPersistentDataContainer().get(key_id, PersistentDataType.INTEGER) == null) {
                    entity.remove();
                    player.sendMessage(Main.gamedesign_prefix + "Fahrzeug wurde gelöscht.");
                }
            } else if (entity instanceof ArmorStand) {
                NamespacedKey key_id = new NamespacedKey(Main.plugin, "id");
                if (entity.getPersistentDataContainer().get(key_id, PersistentDataType.INTEGER) == null) {
                    entity.remove();
                    player.sendMessage(Main.gamedesign_prefix + "Rüstungsständer wurde gelöscht.");
                }
            } else if (entity instanceof Arrow) {
                entity.remove();
                player.sendMessage(Main.gamedesign_prefix + "Ein Pfeil wurde gelöscht.");
            }
        }
        return true;
    }
}