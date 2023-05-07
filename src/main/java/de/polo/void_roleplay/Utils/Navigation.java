package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.GasStationData;
import de.polo.void_roleplay.DataStorage.LocationData;
import de.polo.void_roleplay.DataStorage.NaviData;
import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class Navigation implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getVariable("navi") == null) {
            if (args.length >= 1) {
                if (args.length >= 2) {
                    createNaviByCord(player, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                } else {
                    createNavi(player, args[0]);
                }
            } else {
                Inventory inv = Bukkit.createInventory(player, 27, "§8 » §6GPS");
                int i = 0;
                for (NaviData naviData : LocationManager.naviDataMap.values()) {
                    if (naviData.isGroup()) {
                        inv.setItem(i, ItemManager.createItem(naviData.getItem(), 1, 0, naviData.getName().replace("&", "§"), null));
                        ItemMeta meta = inv.getItem(i).getItemMeta();
                        meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER, naviData.getId());
                        inv.getItem(i).setItemMeta(meta);
                        i++;
                    }
                }
                playerData.setVariable("current_inventory", "navi");
                player.openInventory(inv);
            }
        } else {
            playerData.setVariable("navi", null);
            player.sendMessage("§8[§6GPS§8]§e Du hast deine Route gelöscht.");
        }
        return false;
    }

    public static void createNaviByCord(Player player, int x, int y, int z) {
        final double length = 5.0;
        final double increment = 0.5;
        final Location targetLocation = new Location(player.getWorld(), x, y, z);
        PlayerManager.playerDataMap.get(player.getUniqueId().toString()).setVariable("navi", "ja");
        BukkitTask particleTask = new BukkitRunnable() {
            double progress = 0.0;

            @Override
            public void run() {
                if (PlayerManager.playerDataMap.get(player.getUniqueId().toString()).getVariable("navi") != null) {
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
                        player.sendMessage("§8[§6GPS§8]§e Du hast dein Ziel erreicht.");
                        PlayerManager.playerDataMap.get(player.getUniqueId().toString()).setVariable("navi", null);
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    public static void createNavi(Player player, String nav) {
        for (LocationData locationData : LocationManager.locationDataMap.values()) {
            if (locationData.getName().equalsIgnoreCase(" " + nav)) {
                PlayerManager.playerDataMap.get(player.getUniqueId().toString()).setVariable("navi", "ja");
                player.sendMessage("§8[§6GPS§8]§7 Du hast eine Route zu §c" + nav + "§7 gesetzt.");
                final double length = 5.0;
                final double increment = 0.5;
                final Location targetLocation = new Location(Bukkit.getWorld(locationData.getWelt()), locationData.getX(), locationData.getY(), locationData.getZ());
                BukkitTask particleTask = new BukkitRunnable() {
                    double progress = 0.0;

                    @Override
                    public void run() {
                        if (PlayerManager.playerDataMap.get(player.getUniqueId().toString()).getVariable("navi") != null) {
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
                                player.sendMessage("§8[§6GPS§8]§e Du hast dein Ziel erreicht.");
                                PlayerManager.playerDataMap.get(player.getUniqueId().toString()).setVariable("navi", null);
                            }
                        } else {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(Main.getInstance(), 0L, 1L);
            }
        }
    }
}
