package de.polo.voidroleplay.manager;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.TabCompletion;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.storage.LocationData;
import de.polo.voidroleplay.storage.NaviData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.RegisteredBlock;
import de.polo.voidroleplay.game.events.NaviReachEvent;
import de.polo.voidroleplay.game.events.SubmitChatEvent;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.player.SoundManager;
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

public class NavigationManager implements CommandExecutor, TabCompleter, Listener {
    private final PlayerManager playerManager;

    public NavigationManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        Main.registerCommand("navi", this);
        Main.addTabCompleter("navi", this);
    }

    public static NaviData getNearestNaviPoint(Location location) {
        NaviData nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (NaviData data : LocationManager.naviDataMap.values()) {
            if (data.getLocation() == null) continue;
            if (data.isGroup()) continue;
            Location dataLocation = Main.getInstance().locationManager.getLocation(data.getLocation());
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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getVariable("navi") == null) {
            if (args.length >= 1) {
                if (args[0].contains("haus:")) {
                    try {
                        int number = Integer.parseInt(args[0].replace("haus:", "").replace(" ", ""));
                        for (RegisteredBlock registeredBlock : Main.getInstance().blockManager.getBlocks()) {
                            if (registeredBlock.getInfo() == null) {
                                System.out.println("Info is null");
                                continue;
                            }
                            if (!registeredBlock.getInfo().equalsIgnoreCase("house")) {
                                continue;
                            }
                            if (registeredBlock.getInfoValue() == null) {
                                System.out.println("InfoValue is null");
                                continue;
                            }
                            try {
                                if (Integer.parseInt(registeredBlock.getInfoValue()) != number) {
                                    // ISSUE VRP-10004: fixed to much log spam
                                    continue;
                                }
                            } catch (NumberFormatException e) {
                                // ISSUE VRP-10004: fixed to much log spam
                                continue;
                            }
                            createNaviByCord(player, (int) registeredBlock.getLocation().getX(), (int) registeredBlock.getLocation().getY(), (int) registeredBlock.getLocation().getZ());
                            player.sendMessage("§8[§eGPS§8]§7 Du hast ein Navi zu Haus " + number + " gemacht");
                            return false;
                        }
                        player.sendMessage(Prefix.ERROR + "Hausnummer nicht gefunden");
                    } catch (NumberFormatException ex) {
                        player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /navi haus:[NUMMER]");
                    }
                }

                if (args[0].contains("atm")) {
                    try {
                        double closestDistance = Double.MAX_VALUE;  // Initialisiert mit einem sehr großen Wert
                        RegisteredBlock closestAtm = null;

                        for (RegisteredBlock registeredBlock : Main.getInstance().blockManager.getBlocks()) {
                            if (registeredBlock.getInfo() == null) {
                                continue;
                            }
                            if (!registeredBlock.getInfo().equalsIgnoreCase("atm")) {
                                continue;
                            }

                            double distance = player.getLocation().distance(registeredBlock.getLocation());

                            if (distance < closestDistance) {
                                closestDistance = distance;
                                closestAtm = registeredBlock;
                            }
                        }

                        if (closestAtm != null) {
                            createNaviByCord(player, (int) closestAtm.getLocation().getX(), (int) closestAtm.getLocation().getY(), (int) closestAtm.getLocation().getZ());
                            player.sendMessage("§8[§eGPS§8]§7 Du hast den nächsten ATM markiert");
                        } else {
                            player.sendMessage(Prefix.ERROR + "Kein ATM gefunden.");
                        }
                    } catch (NumberFormatException ex) {
                        player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /navi haus:[NUMMER]");
                    }
                }

                if (args.length >= 3) {
                    createNaviByCord(player, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                } else {
                    String nav = Utils.stringArrayToString(args);
                    NaviData data = LocationManager.naviDataMap.values().stream()
                            .filter(x -> !x.isGroup() && x.getClearName().equalsIgnoreCase(nav))
                            .findFirst()
                            .orElse(null);
                    if (data == null) {
                        player.sendMessage(Component.text(Prefix.ERROR + "Der Punkte wurde nicht gefunden."));
                        return false;
                    }
                    createNavi(player, data.getLocation(), false);
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
        InventoryManager inventory = new InventoryManager(player, 36, "§8 » §6GPS Navigator", true, false);
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
                            InventoryManager naviInventory = new InventoryManager(player, 36, "§8 » " + naviData.getName().replace("&", "§"), true, false);

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
                                            Main.getInstance().utils.navigationManager.createNaviByCord(player, locationData.getX(), locationData.getY(), locationData.getZ());
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
                            Main.getInstance().utils.navigationManager.createNaviByCord(player, locationData.getX(), locationData.getY(), locationData.getZ());
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
                        Bukkit.getPluginManager().callEvent(new NaviReachEvent(player, playerManager.getPlayerData(player.getUniqueId()).getVariable("navi"), targetLocation));
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
                                Bukkit.getPluginManager().callEvent(new NaviReachEvent(player, playerManager.getPlayerData(player.getUniqueId()).getVariable("navi"), targetLocation));
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
                                Bukkit.getPluginManager().callEvent(new NaviReachEvent(player, playerManager.getPlayerData(player.getUniqueId()).getVariable("navi"), targetLocation));
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
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, LocationManager.naviDataMap.values().stream().filter(x -> !x.isGroup())
                        .map(NaviData::getClearName).toList())
                .build();
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
