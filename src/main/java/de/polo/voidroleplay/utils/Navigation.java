package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.dataStorage.*;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.playerUtils.SoundManager;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.game.events.NaviReachEvent;
import de.polo.voidroleplay.game.events.SubmitChatEvent;
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

    public static NaviData getNearestNaviPoint(Location location) {
        NaviData nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (NaviData data : LocationManager.naviDataMap.values()) {
            if (data.getLocation() == null) continue;
            Location dataLocation = Main.getInstance().locationManager.getLocation(data.getLocation());
            double distance = dataLocation.distance(location);

            if (nearest == null || distance < nearestDistance) {
                nearest = data;
                nearestDistance = distance;
            }
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
                                    System.out.println("InfoValue does not equal number");
                                    continue;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("InfoValue is not a valid number");
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
                            naviInventory.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
                                @Override
                                public void onClick(InventoryClickEvent event) {
                                    openNavi(player, null);
                                }
                            });
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
        if (search == null) {
            inventory.setItem(new CustomItem(22, ItemManager.createItem(Material.CLOCK, 1, 0, "§7GPS Punkt suchen...")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerData.setVariable("chatblock", "gpssearch");
                    player.sendMessage("§8[§eGPS§8]§7 Gib nun den gesuchten GPS Punkt ein.");
                    player.closeInventory();
                }
            });
        } else {
            inventory.setItem(new CustomItem(22, ItemManager.createItem(Material.BARRIER, 1, 0, "§cSuche löschen")) {
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
                    }
                }
                if (!isNavi) {
                    player.sendMessage(Prefix.ERROR + "Der Navipunkt wurde nicht gefunden.");
                    return;
                };
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
