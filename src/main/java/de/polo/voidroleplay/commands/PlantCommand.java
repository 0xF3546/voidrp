package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.faction.plants.Plant;
import de.polo.voidroleplay.utils.GamePlay.GamePlay;
import de.polo.voidroleplay.utils.LocationManager;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
