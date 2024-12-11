package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.events.SecondTickEvent;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class CatchFishCommand extends CommandBase implements Listener {
    private final HashMap<Player, LocalDateTime> caughts = new HashMap<>();
    private final String PREFIX = "§8[§bHochseefischer§8]§7 ";
    public CatchFishCommand(@NotNull CommandMeta meta) {
        super(meta);

        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @Override
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (caughts.get(player) != null) {
            player.sendMessage(PREFIX + "Warte noch einen Moment.");
            return;
        }
        Location nearestLocation = null;
        double nearestDistance = Double.MAX_VALUE;
        List<Location> playerLocations = playerData.getVariable("job::hochseefischer::locations");
        if (playerLocations == null) {
            playerLocations = new ObjectArrayList<>();
            updateLocations(playerData, playerLocations);
        }

        for (Location location : HochseefischerCommand.getLocations()) {
            double distance = player.getLocation().distance(location);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestLocation = location;
            }
        }
        if (playerLocations.contains(nearestLocation)) {
            player.sendMessage(PREFIX + "Du warst an der Stelle bereits fischen.");
            return;
        }
        playerLocations.add(nearestLocation);
        catchFish(player);
        updateLocations(playerData, playerLocations);
    }

    private void catchFish(Player player) {
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
        PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player);
        int random = Main.random(10, 20);
        playerData.setVariable("hochseefischer_kg", (int) playerData.getVariable("hochseefischer_kg") + random);
        player.sendMessage(PREFIX + "Du hast §6" + random + "kg Fisch gefangen.");
    }

    @EventHandler
    public void onSecond(SecondTickEvent event) {
        for (Player player : HochseefischerCommand.getPlayers()) {
            PlayerData playerData = Main.getInstance().playerManager.getPlayerData(player);
            player.sendActionBar(Component.text("§bEs befinden sich §6" + playerData.getVariable("hochseefischer_kg") + "kg§b Fisch im Boot."));
            if (caughts.get(player) != null) continue;
            if (!caughts.get(player).plusSeconds(20).isAfter(Utils.getTime())) continue;
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
