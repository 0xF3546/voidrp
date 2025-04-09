package de.polo.core.location.services.impl;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.core.Main;
import de.polo.api.VoidAPI;
import de.polo.core.handler.TabCompletion;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.storage.LocationData;
import de.polo.core.storage.NaviData;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.game.events.NaviReachEvent;
import de.polo.core.game.events.SubmitChatEvent;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.player.SoundManager;
import net.kyori.adventure.text.Component;
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

import java.util.List;

import static de.polo.core.Main.locationManager;

public class NavigationManager implements Listener {
    private final PlayerManager playerManager;

    public NavigationManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    public static NaviData getNearestNaviPoint(Location location) {
        NaviData nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (NaviData data : LocationManager.naviDataMap.values()) {
            if (data.getLocation() == null) continue;
            if (data.isGroup()) continue;
            Location dataLocation = locationManager.getLocation(data.getLocation());
            if (dataLocation == null) continue;
            System.out.println(dataLocation);
            double distance = dataLocation.distance(location);

            if (nearest == null || distance < nearestDistance) {
                nearest = data;
                nearestDistance = distance;
            }
        }
        if (nearest == null) {
            nearest = LocationManager.naviDataMap.values().stream().findFirst().orElse(null);
        }
        return nearest;
    }

    public void openNavi(Player player, String search) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        InventoryManager inventory = new InventoryManager(player, 36, Component.text("§8 » §6GPS Navigator"), true, false);
        playerData.setVariable("originClass", this);

        int i = 10;
        for (NaviData naviData : LocationManager.naviDataMap.values()) {
            if (search == null) {
                if (naviData.isGroup()) {
                    ItemStack stack = ItemManager.createItem(naviData.getItem(), 1, 0, "§e▶ " + naviData.getName().replace("&", "§"));
                    inventory.setItem(new CustomItem(i, stack) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            SoundManager.clickSound(player);
                            InventoryManager naviInventory = new InventoryManager(player, 36, Component.text("§8 » " + naviData.getName().replace("&", "§")), true, false);

                            naviInventory.setItem(new CustomItem(27, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§c◀ Zurück")) {
                                @Override
                                public void onClick(InventoryClickEvent event) {
                                    openNavi(player, null);
                                }
                            });

                            int j = 10;
                            for (NaviData newNavi : LocationManager.naviDataMap.values()) {
                                if (newNavi.getGroup().equalsIgnoreCase(naviData.getGroup()) && !newNavi.isGroup()) {
                                    ItemStack item = ItemManager.createItem(newNavi.getItem(), 1, 0, newNavi.getName().replace("&", "§"),
                                            "§7 ➔ §e" + (int) Main.getInstance().locationManager.getDistanceBetweenCoords(player, newNavi.getLocation()) + "m");
                                    naviInventory.setItem(new CustomItem(j, item) {
                                        @Override
                                        public void onClick(InventoryClickEvent event) {
                                            player.sendMessage("\u00a78[\u00a76GPS\u00a78]\u00a77 Route zu " + newNavi.getName().replace("&", "§") + "§7 gesetzt.");
                                            LocationData locationData = LocationManager.locationDataMap.get(newNavi.getLocation());
                                            createNaviByCord(player, locationData.getX(), locationData.getY(), locationData.getZ());
                                            player.closeInventory();
                                        }
                                    });
                                    j++;
                                    if (j % 9 == 8) j += 2;
                                }
                            }
                        }
                    });
                    i++;
                    if (i % 9 == 8) i += 2;
                }
            } else {
                if (naviData.getName().toLowerCase().contains(search.toLowerCase()) && !naviData.isGroup()) {
                    ItemStack stack = ItemManager.createItem(naviData.getItem(), 1, 0, "§e▶ " + naviData.getName().replace("&", "§"));
                    inventory.setItem(new CustomItem(i, stack) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            player.sendMessage("\u00a78[\u00a76GPS\u00a78]\u00a77 Route zu " + naviData.getName().replace("&", "§") + "§7 gesetzt.");
                            LocationData locationData = LocationManager.locationDataMap.get(naviData.getLocation());
                            createNaviByCord(player, locationData.getX(), locationData.getY(), locationData.getZ());
                            player.closeInventory();
                        }
                    });
                    i++;
                    if (i % 9 == 8) i += 2;
                }
            }
        }

        if (search == null) {
            inventory.setItem(new CustomItem(31, ItemManager.createItem(Material.COMPASS, 1, 0, "§6GPS Punkt suchen...", "§7Klicke hier, um eine Suche zu starten!")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerData.setVariable("chatblock", "gpssearch");
                    player.sendMessage("§8[§6GPS§8]§7 Gib nun den gesuchten GPS Punkt ein.");
                    player.closeInventory();
                }
            });
        } else {
            inventory.setItem(new CustomItem(31, ItemManager.createItem(Material.BARRIER, 1, 0, "§cSuche löschen", "§7Klicke hier, um die Suche zu beenden!")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openNavi(player, null);
                }
            });
        }
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
                    spawnParticle(player, targetLocation, Particle.REDSTONE);
                    String actionBarText = "§8 » §6Noch " + (int) Math.floor(player.getLocation().distance(targetLocation)) + " Meter§8 « ";
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
                    if (player.getLocation().distance(targetLocation) <= 5) {
                        this.cancel();
                        Bukkit.getPluginManager().callEvent(new NaviReachEvent(VoidAPI.getPlayer(player), playerManager.getPlayerData(player.getUniqueId()).getVariable("navi"), targetLocation));
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
                boolean isNavi = false;
                for (NaviData naviData : LocationManager.naviDataMap.values()) {
                    if (naviData.getLocation() == null) continue;
                    if (naviData.getLocation().equalsIgnoreCase(locationData.getName())) {
                        isNavi = true;
                        break;
                    }
                }
                if (!isNavi) {
                    player.sendMessage(Prefix.ERROR + "Der Navipunkt wurde nicht gefunden.");
                    return;
                }
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
                            spawnParticle(player, targetLocation, Particle.REDSTONE);
                            if (player.getLocation().distance(targetLocation) <= 5) {
                                this.cancel();
                                player.sendMessage("§8[§6GPS§8]§e Du hast dein Ziel erreicht.");
                                Bukkit.getPluginManager().callEvent(new NaviReachEvent(VoidAPI.getPlayer(player), playerManager.getPlayerData(player.getUniqueId()).getVariable("navi"), targetLocation));
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

    public void createNaviByLocation(Player player, String nav) {
        for (LocationData locationData : LocationManager.locationDataMap.values()) {
            if (locationData.getName().equalsIgnoreCase(nav)) {
                playerManager.getPlayerData(player.getUniqueId()).setVariable("navi", nav);
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
                            spawnParticle(player, targetLocation, Particle.REDSTONE);
                            if (player.getLocation().distance(targetLocation) <= 5) {
                                this.cancel();
                                player.sendMessage("§8[§6GPS§8]§e Du hast dein Ziel erreicht.");
                                Bukkit.getPluginManager().callEvent(new NaviReachEvent(VoidAPI.getPlayer(player), playerManager.getPlayerData(player.getUniqueId()).getVariable("navi"), targetLocation));
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

    private void spawnParticle(Player player, Location location, Particle particle) {
        for (int d = 0; d <= 90; d += 1) {
            Location particleLoc = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
            particleLoc.setX(location.getX() + Math.cos(d) * 2);
            particleLoc.setZ(location.getZ() + Math.sin(d) * 2);
            player.spawnParticle(particle, particleLoc, 1, new Particle.DustOptions(Color.WHITE, 5));
        }

    }
}
