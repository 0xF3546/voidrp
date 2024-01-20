package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.Apotheke;
import de.polo.metropiacity.dataStorage.Plant;
import de.polo.metropiacity.utils.GamePlay.GamePlay;
import de.polo.metropiacity.utils.LocationManager;
import de.polo.metropiacity.utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

public class PlantCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    private final GamePlay gamePlay;
    public PlantCommand(PlayerManager playerManager, LocationManager locationManager, GamePlay gamePlay) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.gamePlay = gamePlay;
        Main.registerCommand("plant", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        for (Plant plant : gamePlay.plant.getPlants()) {
            if (locationManager.getLocation("plant-" + plant.getId()) == null) {
                player.sendMessage(Main.error + "Du bist nicht in der nähe einer Plantage!");
                return false;
            }
            if (locationManager.getDistanceBetweenCoords(player, "plant-" + plant.getId()) < 5) {
                gamePlay.plant.openPlant(player, plant.getId());
                return false;
            }
        }
        player.sendMessage(Main.error + "Du bist nicht in der nähe einer Plantage!");
        return false;
    }
}
