package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.GasStationData;
import de.polo.void_roleplay.DataStorage.LocationData;
import de.polo.void_roleplay.Main;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class Navigation implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            if (args.length >= 2) {
                createNaviByCord(player, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            } else {
                createNavi(player, args[0]);
            }
        } else {
            Inventory inv = Bukkit.createInventory(player, 27, "§8 » §6Navi");
            player.openInventory(inv);
        }
        return false;
    }

    public static void createNaviByCord(Player player, int x, int y, int z) {
        final double length = 5.0;
        final double increment = 0.5;
        final Location targetLocation = new Location(player.getWorld(), x, y, z);
        BukkitTask particleTask = new BukkitRunnable() {
            double progress = 0.0;

            @Override
            public void run() {
                Vector direction = targetLocation.clone().subtract(player.getEyeLocation()).toVector().normalize();
                Location particleLocation = player.getEyeLocation().clone().add(direction.clone().multiply(progress));
                player.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0.0, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.ORANGE, 1));
                progress += increment;
                if (progress >= length) {
                    progress = 0.0;
                }
                String actionBarText = "§8 » §6Noch " + (int) Math.floor(player.getLocation().distance(targetLocation)) + " Meter§8 « ";
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
                if (player.getLocation().distance(targetLocation) <= 5) {
                    this.cancel();
                    player.sendMessage("&8[§6Navi§8]§7 Du hast dein Ziel erreicht.");
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    public static void createNavi(Player player, String nav) {
        for (LocationData locationData : LocationManager.locationDataMap.values()) {
            if (locationData.getName().equalsIgnoreCase(" " + nav)) {
                player.sendMessage("&8[§6Navi§8]§7 Du hast eine Route zu §c" + nav + "§7 gesetzt.");
                final double length = 5.0;
                final double increment = 0.5;
                final Location targetLocation = new Location(Bukkit.getWorld(locationData.getWelt()), locationData.getX(), locationData.getY(), locationData.getZ());
                BukkitTask particleTask = new BukkitRunnable() {
                    double progress = 0.0;

                    @Override
                    public void run() {
                        Vector direction = targetLocation.clone().subtract(player.getEyeLocation()).toVector().normalize();
                        Location particleLocation = player.getEyeLocation().clone().add(direction.clone().multiply(progress));
                        player.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0.0, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.ORANGE, 1));
                        progress += increment;
                        if (progress >= length) {
                            progress = 0.0;
                        }
                        String actionBarText = "§8 » §6Noch " + (int) Math.floor(player.getLocation().distance(targetLocation)) + " Meter§8 « ";
                        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
                        if (player.getLocation().distance(targetLocation) <= 5) {
                            this.cancel();
                            player.sendMessage("&8[§6Navi§8]§7 Du hast dein Ziel erreicht.");
                        }
                    }
                }.runTaskTimer(Main.getInstance(), 0L, 1L);
            }
        }
    }
}
