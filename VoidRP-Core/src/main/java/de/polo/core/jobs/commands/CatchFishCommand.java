package de.polo.core.jobs.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.game.events.SecondTickEvent;
import de.polo.core.handler.CommandBase;
import de.polo.core.location.services.NavigationService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
@CommandBase.CommandMeta(name = "catchfish")
public class CatchFishCommand extends CommandBase implements Listener {
    private final HashMap<VoidPlayer, LocalDateTime> caughts = new HashMap<>();
    private final String PREFIX = "§8[§bHochseefischer§8]§7 ";
    public CatchFishCommand(@NotNull CommandMeta meta) {
        super(meta);

        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (caughts.get(player) != null) {
            player.sendMessage(PREFIX + "Warte noch einen Moment.");
            return;
        }
        Location nearestLocation = null;
        double nearestDistance = Double.MAX_VALUE;
        List<Location> playerLocations = playerData.getVariable("job::hochseefischer::locations");
        for (Location location : HochseefischerCommand.getLocations()) {
            double distance = player.getLocation().distance(location);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestLocation = location;
            }
        }
        if (nearestLocation == null) {
            return;
        }
        if (nearestLocation.distance(player.getLocation()) > 5) {
            player.sendMessage(Component.text(PREFIX + "Es ist keine Fangstelle in der nähe."));
            return;
        }
        if (playerLocations.contains(nearestLocation)) {
            player.sendMessage(PREFIX + "Du warst an der Stelle bereits fischen.");
            return;
        }
        playerLocations.add(nearestLocation);
        catchFish(player);
        updateLocations(playerData, playerLocations);
    }

    private void catchFish(VoidPlayer player) {
        caughts.put(player, Utils.getTime());
        player.sendMessage(Component.text(PREFIX + "Du hast dein Fangnetz ausgeworfen."));

        /*Vector direction = player.getLocation().getDirection().normalize();
        Location startLocation = player.getLocation().add(direction.multiply(2));
        World world = player.getWorld();
        for (int i = 0; i < 10; i++) {
            Location webLocation = startLocation.clone().add(direction.multiply(i));

            if (webLocation.getBlock().getType() == Material.AIR) {
                world.spawnEntity(webLocation, EntityType.COBWEB);
            }
        }*/
    }


    private void updateLocations(PlayerData playerData, List<Location> locations) {
        playerData.setVariable("job::hochseefischer::locations", locations);
    }

    private void caughtFish(Player player) {
        PlayerData playerData = Main.playerManager.getPlayerData(player);
        int random = Utils.random(10, 20);
        playerData.setVariable("hochseefischer_kg", (int) playerData.getVariable("hochseefischer_kg") + random);
        player.sendMessage(PREFIX + "Du hast §6" + random + "kg§7 Fisch gefangen.");
        Location location = HochseefischerCommand.getNearstLocation(player);
        if (location == null) {
            player.sendMessage(PREFIX + "Du bist fertig. Gehe nun zurück zum Hochseefischer.");
            return;
        }
        NavigationService navigationService = VoidAPI.getService(NavigationService.class);
        navigationService.createNaviByCord(player, (int) location.getX(), (int) location.getY(), (int) location.getZ());
    }

    @EventHandler
    public void onSecond(SecondTickEvent event) {
        for (Player player : HochseefischerCommand.getPlayers()) {
            PlayerData playerData = Main.playerManager.getPlayerData(player);
            player.sendActionBar(Component.text("§3Es befinden sich " + playerData.getVariable("hochseefischer_kg") + "kg Fisch im Boot."));
            if (caughts.get(player) == null) continue;
            if (!caughts.get(player).plusSeconds(20).isBefore(Utils.getTime())) continue;
            caughts.remove(player);
            caughtFish(player);
        }
    }

    /*@EventHandler
    public void onEntityCollision(EntityDamageEvent event) {
        if (event.getEntity().getType() == EntityType.WEB) {
            Block block = event.getEntity().getLocation().getBlock();

            // Wenn die Spinnenwebe einen Block trifft, entferne sie
            if (block.getType() != Material.AIR) {
                event.getEntity().remove(); // Entfernt die Spinnenwebe (Entity)
            }
        }
    }*/
}
