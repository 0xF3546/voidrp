package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.game.faction.plants.Plant;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.GamePlay.GamePlay;
import de.polo.voidroleplay.utils.LocationManager;
import de.polo.voidroleplay.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlantagenCommand implements CommandExecutor {
    private final GamePlay gamePlay;
    private final Utils utils;
    private final FactionManager factionManager;
    private final LocationManager locationManager;
    public PlantagenCommand(GamePlay gamePlay, Utils utils, FactionManager factionManager, LocationManager locationManager) {
        this.gamePlay = gamePlay;
        this.utils = utils;
        this.factionManager = factionManager;
        this.locationManager = locationManager;
        Main.registerCommand("plants", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        player.sendMessage("   §7===§8[§2Plantagen§8]§7===");
        for (Plant plant : gamePlay.plant.getPlants()) {
            FactionData factionData = factionManager.getFactionData(plant.getOwner());
            String attackable = "";
            long minutesDifference = gamePlay.plant.getMinuteDifference(plant);
            if (minutesDifference < 360 && minutesDifference >= 0) {
                attackable = "§8 - §c" + minutesDifference + "min";
            }
            Location location = locationManager.getLocation("plant-" + plant.getId());
            if (location != null) {
                TextComponent message = new TextComponent("§8 ➥ §2Plantage-" + plant.getId() + " (" + plant.getMultiplier() + "x) §8 | §" + factionData.getPrimaryColor() + factionData.getFullname() + attackable);
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) location.getX() + " " + (int) location.getY() + " " + (int) location.getZ()));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));
                player.spigot().sendMessage(message);
            } else {
                player.sendMessage("§8 ➥ §2Plantage-" + plant.getId() + " (" + plant.getMultiplier() + "x) §8 | §" + factionData.getPrimaryColor() + factionData.getName() + attackable);
            }
        }
        return false;
    }
}
