package de.polo.metropiacity.utils;

import de.polo.metropiacity.dataStorage.*;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.playerUtils.SoundManager;
import de.polo.metropiacity.utils.InventoryManager.CustomItem;
import de.polo.metropiacity.utils.InventoryManager.InventoryManager;
import de.polo.metropiacity.utils.events.NaviReachEvent;
import de.polo.metropiacity.utils.events.SubmitChatEvent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Navigation implements CommandExecutor, TabCompleter, Listener {
    private final PlayerManager playerManager;
    public Navigation(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        Main.registerCommand("navi", this);
        Main.addTabCompeter("navi", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getVariable("navi") == null) {
            if (args.length >= 1) {
                if (args.length >= 3) {
                    createNaviByCord(player, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                } else {
                    StringBuilder nav = new StringBuilder(args[0]);
                    for (int i = 1; i < args.length; i++) {
                        nav.append(" ").append(args[i]);
                    }
                    createNavi(player, nav.toString(), false);
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
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        InventoryManager inventory = new InventoryManager(player, 27, "§8 » §6GPS", true, false);
        playerData.setVariable("originClass", this);
        int i = 0;
        for (NaviData naviData : LocationManager.naviDataMap.values()) {
            if (search == null) {
                if (naviData.isGroup()) {
                    ItemStack stack = ItemManager.createItem(naviData.getItem(), 1, 0, naviData.getName().replace("&", "§"));
                    inventory.setItem(new CustomItem(i, stack) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            SoundManager.clickSound(player);
                            InventoryManager naviInventory = new InventoryManager(player, 27, "§8 » " + naviData.getName().replace("&", "§"), true, false);
                            int i = 0;
                            for (NaviData newNavi : LocationManager.naviDataMap.values()) {
                                if (newNavi.getGroup().equalsIgnoreCase(naviData.getGroup()) && !newNavi.isGroup()) {
                                    ItemStack stack = ItemManager.createItem(newNavi.getItem(), 1, 0, newNavi.getName().replace("&", "§"), "§7 ➥ §e" + (int) Main.getInstance().locationManager.getDistanceBetweenCoords(player, newNavi.getLocation()) + "m");
                                    naviInventory.setItem(new CustomItem(i, stack) {
                                        @Override
                                        public void onClick(InventoryClickEvent event) {
                                            player.sendMessage("§8[§6GPS§8]§7 Du hast eine Route zu " + newNavi.getName().replace("&", "§") + "§7 gesetzt.");
                                            LocationData locationData = LocationManager.locationDataMap.get(newNavi.getLocation());
                                            Main.getInstance().utils.navigation.createNaviByCord(player, locationData.getX(), locationData.getY(), locationData.getZ());
                                            player.closeInventory();
                                        }
                                    });
                                    i++;
                                }
                            }
                        }
                    });
                    i++;
                }
            } else {
                if (naviData.getName().toLowerCase().contains(search.toLowerCase())) {
                    if (!naviData.isGroup()) {
                        ItemStack stack = ItemManager.createItem(naviData.getItem(), 1, 0, naviData.getName().replace("&", "§"));
                        inventory.setItem(new CustomItem(i, stack) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                                player.sendMessage("§8[§6GPS§8]§7 Du hast eine Route zu " + naviData.getName().replace("&", "§") + "§7 gesetzt.");
                                LocationData locationData = LocationManager.locationDataMap.get(naviData.getLocation());
                                Main.getInstance().utils.navigation.createNaviByCord(player, locationData.getX(), locationData.getY(), locationData.getZ());
                                player.closeInventory();
                            }
                        });
                        i++;
                    }
                }
            }
        }
        inventory.setItem(new CustomItem(22, ItemManager.createItem(Material.CLOCK, 1, 0, "§7GPS Punkt suchen...")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("chatblock", "gpssearch");
                player.sendMessage("§8[§eGPS§8]§7 Gib nun den gesuchten GPS Punkt ein.");
                player.closeInventory();
            }
        });
    }

    public void createNaviByCord(Player player, int x, int y, int z) {
        final double length = 5.0;
        final double increment = 0.5;
        final Location targetLocation = new Location(player.getWorld(), x, y, z);
        playerManager.getPlayerData(player.getUniqueId()).setVariable("navi", "ja");
        BukkitTask particleTask = new BukkitRunnable() {
            double progress = 0.0;

            @Override
            public void run() {
                if (!player.isOnline()) this.cancel();
                if (playerManager.getPlayerData(player.getUniqueId()).getVariable("navi") != null) {
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
                        Bukkit.getPluginManager().callEvent(new NaviReachEvent(player, playerManager.getPlayerData(player.getUniqueId()).getVariable("navi")));
                        player.sendMessage("§8[§6GPS§8]§e Du hast dein Ziel erreicht.");
                        playerManager.getPlayerData(player.getUniqueId()).setVariable("navi", null);
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    public void createNavi(Player player, String nav, boolean silent) {
        for (LocationData locationData : LocationManager.locationDataMap.values()) {
            if (locationData.getName().equalsIgnoreCase(nav)) {
                playerManager.getPlayerData(player.getUniqueId()).setVariable("navi", nav);
                if (!silent) player.sendMessage("§8[§6GPS§8]§7 Du hast eine Route zu §c" + nav + "§7 gesetzt.");
                final double length = 5.0;
                final double increment = 0.5;
                final Location targetLocation = new Location(Bukkit.getWorld(locationData.getWelt()), locationData.getX(), locationData.getY(), locationData.getZ());
                BukkitTask particleTask = new BukkitRunnable() {
                    double progress = 0.0;

                    @Override
                    public void run() {
                        if (!player.isOnline()) this.cancel();
                        if (playerManager.getPlayerData(player.getUniqueId()).getVariable("navi") != null) {
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
                                Bukkit.getPluginManager().callEvent(new NaviReachEvent(player, playerManager.getPlayerData(player.getUniqueId()).getVariable("navi")));
                                playerManager.getPlayerData(player.getUniqueId()).setVariable("navi", null);
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
            event.end();
        }
    }

}
