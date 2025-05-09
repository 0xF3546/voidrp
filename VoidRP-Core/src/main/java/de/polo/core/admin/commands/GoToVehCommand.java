package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.game.base.vehicle.PlayerVehicleData;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.vehicles.services.VehicleService;
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
        VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
        if (!voidPlayer.isAduty()) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht im Admindienst!");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /gotovehicle [ID]");
            return false;
        }
        VehicleService vehicleService = VoidAPI.getService(VehicleService.class);
        PlayerVehicleData playerVehicleData = vehicleService.getPlayerVehicleById(Integer.parseInt(args[0])).orElse(null);
        if (playerVehicleData == null) {
            player.sendMessage(Prefix.ERROR + "Das Fahrzeug mit der ID §l" + args[0] + "§7 existiert nicht.");
            return false;
        }
        player.teleport(playerVehicleData.getLocation());
        player.sendMessage(Prefix.ADMIN + "Du hast dich zum Fahrzeug mit der ID §l" + args[0] + "§7 teleportiert.");
        return false;
    }
}
