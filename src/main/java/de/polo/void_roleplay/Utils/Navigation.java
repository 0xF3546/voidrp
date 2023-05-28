package de.polo.void_roleplay.Utils;

import de.polo.void_roleplay.DataStorage.*;
import de.polo.void_roleplay.Main;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Navigation implements CommandExecutor, TabCompleter, Listener {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getVariable("navi") == null) {
            if (args.length >= 1) {
                if (args.length >= 2) {
                    createNaviByCord(player, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                } else {
                    createNavi(player, args[0], false);
                }
            } else {
                openNavi(player, null);
            }
        } else {
            playerData.setVariable("navi", null);
            player.sendMessage("§8[§6GPS§8]§e Du hast deine Route gelöscht.");
        }
        return false;
    }

    public void openNavi(Player player, String search) {
        System.out.println("öffne inv");
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        Inventory inv = Bukkit.createInventory(player, 27, "§8 » §6GPS");
        int i = 0;
        for (NaviData naviData : LocationManager.naviDataMap.values()) {
            if (search == null) {
                if (naviData.isGroup()) {
                    inv.setItem(i, ItemManager.createItem(naviData.getItem(), 1, 0, naviData.getName().replace("&", "§"), null));
                    ItemMeta meta = inv.getItem(i).getItemMeta();
                    meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER, naviData.getId());
                    inv.getItem(i).setItemMeta(meta);
                    i++;
                }
            } else {
                if (naviData.getName().toLowerCase().contains(search.toLowerCase())) {
                    if (!naviData.isGroup()) {
                        inv.setItem(i, ItemManager.createItem(naviData.getItem(), 1, 0, naviData.getName().replace("&", "§"), null));
                        ItemMeta meta = inv.getItem(i).getItemMeta();
                        meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "id"), PersistentDataType.INTEGER, naviData.getId());
                        inv.getItem(i).setItemMeta(meta);
                        i++;
                    }
                }
            }
        }
        inv.setItem(22, ItemManager.createItem(Material.CLOCK, 1, 0, "§7GPS Punkt suchen...", null));
        playerData.setVariable("current_inventory", "navi");
        player.openInventory(inv);
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
                if (!player.isOnline()) this.cancel();
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
                        Bukkit.getPluginManager().callEvent(new NaviReachEvent(player, PlayerManager.playerDataMap.get(player.getUniqueId().toString()).getVariable("navi")));
                        player.sendMessage("§8[§6GPS§8]§e Du hast dein Ziel erreicht.");
                        PlayerManager.playerDataMap.get(player.getUniqueId().toString()).setVariable("navi", null);
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    public static void createNavi(Player player, String nav, boolean silent) {
        for (LocationData locationData : LocationManager.locationDataMap.values()) {
            if (locationData.getName().equalsIgnoreCase(nav)) {
                PlayerManager.playerDataMap.get(player.getUniqueId().toString()).setVariable("navi", nav);
                if (!silent) player.sendMessage("§8[§6GPS§8]§7 Du hast eine Route zu §c" + nav + "§7 gesetzt.");
                final double length = 5.0;
                final double increment = 0.5;
                final Location targetLocation = new Location(Bukkit.getWorld(locationData.getWelt()), locationData.getX(), locationData.getY(), locationData.getZ());
                BukkitTask particleTask = new BukkitRunnable() {
                    double progress = 0.0;

                    @Override
                    public void run() {
                        if (!player.isOnline()) this.cancel();
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
                                Bukkit.getPluginManager().callEvent(new NaviReachEvent(player, PlayerManager.playerDataMap.get(player.getUniqueId().toString()).getVariable("navi")));
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

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (NaviData naviData : LocationManager.naviDataMap.values()) {
                if (!naviData.isGroup()) {
                    suggestions.add(naviData.getName().substring(2));
                }
            }

            return suggestions;
        }
        return null;
    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) {
        if (event.getSubmitTo().equals("gpssearch")) {
            if (event.isCancel()) {
                event.end();
                event.sendCancelMessage();
                return;
            }
            openNavi(event.getPlayer(), event.getMessage());
            System.out.println("navi öfffnennn");
            event.end();
        }
    }
}
