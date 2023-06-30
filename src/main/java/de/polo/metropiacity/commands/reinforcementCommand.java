package de.polo.metropiacity.commands;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Utils.FactionManager;
import de.polo.metropiacity.Utils.PlayerManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class reinforcementCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
            if (args.length == 0) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                    if (Objects.equals(playerData.getFaction(), playerData1.getFaction())) {
                        players.sendMessage("§c§lHilfe! §3" + player.getName() + " benötige Unterstützung! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                        TextComponent start = new TextComponent("§8 ➥ ");
                        TextComponent route = new TextComponent("§aRoute");
                        TextComponent mid = new TextComponent("§8 | ");
                        TextComponent toPlayer = new TextComponent("§aUnterwegs!");
                        route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                        route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));

                        toPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reinforcement gotoreinf " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ() + " " + player.getName() + " normal"));
                        toPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oIch bin unterwegs!")));

                        players.spigot().sendMessage(start, route, mid, toPlayer);
                        players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                    }
                }
            } else {
                switch (args[0]) {
                    case "-d":
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                            if (Objects.equals(playerData.getFaction(), playerData1.getFaction())) {
                                players.sendMessage("§c§lDRINGEND HILFE! §3" + player.getName() + " benötige Dringend Unterstützung! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                                TextComponent start = new TextComponent("§8 ➥ ");
                                TextComponent route = new TextComponent("§aRoute");
                                TextComponent mid = new TextComponent("§8 | ");
                                TextComponent toPlayer = new TextComponent("§aUnterwegs!");
                                route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                                route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));

                                toPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reinforcement gotoreinf " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ() + " " + player.getName() + " d"));
                                toPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oIch bin unterwegs!")));

                                players.spigot().sendMessage(start, route, mid, toPlayer);
                                players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                            }
                        }
                        break;
                    case "-p":
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                            if (Objects.equals(playerData.getFaction(), playerData1.getFaction())) {
                                players.sendMessage("§e§lGPS geteilt! §3" + player.getName() + " teile meine Position! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                                TextComponent start = new TextComponent("§8 ➥ ");
                                TextComponent route = new TextComponent("§aRoute");
                                TextComponent mid = new TextComponent("§8 | ");
                                TextComponent toPlayer = new TextComponent("§aUnterwegs!");
                                route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                                route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));

                                toPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reinforcement gotoreinf " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ() + " " + player.getName() + " p"));
                                toPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oIch bin unterwegs!")));

                                players.spigot().sendMessage(start, route, mid, toPlayer);
                                players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                            }
                        }
                        break;
                    case "-ed":
                        if (PlayerManager.isInStaatsFrak(player)) {
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                if (PlayerManager.isInStaatsFrak(players)) {
                                    players.sendMessage("§c§lDRINGEND HILFE! §3" + playerData.getFaction() + " " + player.getName() + " benötige Dringend Unterstützung! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                                    TextComponent start = new TextComponent("§8 ➥ ");
                                    TextComponent route = new TextComponent("§aRoute");
                                    TextComponent mid = new TextComponent("§8 | ");
                                    TextComponent toPlayer = new TextComponent("§aUnterwegs!");
                                    route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                                    route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));

                                    toPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reinforcement gotoreinf " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ() + " " + player.getName() + " dep"));
                                    toPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oIch bin unterwegs!")));

                                    players.spigot().sendMessage(start, route, mid, toPlayer);
                                    players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                                }
                            }
                        } else if (FactionManager.isInBündnis(player)) {
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                if (FactionManager.isInBündnisWith(players, playerData.getFaction())) {
                                    players.sendMessage("§c§lDRINGEND HILFE! §3" + playerData.getFaction() + " " + player.getName() + " benötige Dringend Unterstützung! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                                    TextComponent start = new TextComponent("§8 ➥ ");
                                    TextComponent route = new TextComponent("§aRoute");
                                    TextComponent mid = new TextComponent("§8 | ");
                                    TextComponent toPlayer = new TextComponent("§aUnterwegs!");
                                    route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                                    route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));

                                    toPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reinforcement gotoreinf " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ() + " " + player.getName() + " dep"));
                                    toPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oIch bin unterwegs!")));

                                    players.spigot().sendMessage(start, route, mid, toPlayer);
                                    players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                                }
                            }
                        }
                        break;
                    case "-ep":
                        if (PlayerManager.isInStaatsFrak(player)) {
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                                if (PlayerManager.isInStaatsFrak(players)) {
                                    players.sendMessage("§e§lGPS geteilt! §3" + playerData.getFaction() + " " + player.getName() + " teile meine Position! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                                    TextComponent start = new TextComponent("§8 ➥ ");
                                    TextComponent route = new TextComponent("§aRoute");
                                    TextComponent mid = new TextComponent("§8 | ");
                                    TextComponent toPlayer = new TextComponent("§aUnterwegs!");
                                    route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                                    route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));

                                    toPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reinforcement gotoreinf " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ() + " " + player.getName() + " dep"));
                                    toPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oIch bin unterwegs!")));

                                    players.spigot().sendMessage(start, route, mid, toPlayer);
                                    players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                                }
                            }
                        } else if (FactionManager.isInBündnis(player)) {
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                if (FactionManager.isInBündnisWith(players, playerData.getFaction())) {
                                    players.sendMessage("§e§lGPS geteilt! §3" + playerData.getFaction() + " " + player.getName() + " teile meine Position! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                                    TextComponent start = new TextComponent("§8 ➥ ");
                                    TextComponent route = new TextComponent("§aRoute");
                                    TextComponent mid = new TextComponent("§8 | ");
                                    TextComponent toPlayer = new TextComponent("§aUnterwegs!");
                                    route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                                    route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));

                                    toPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reinforcement gotoreinf " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ() + " " + player.getName() + " dep"));
                                    toPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oIch bin unterwegs!")));

                                    players.spigot().sendMessage(start, route, mid, toPlayer);
                                    players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                                }
                            }
                        }
                        break;
                    case "-e":
                        if (PlayerManager.isInStaatsFrak(player)) {
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                                if (PlayerManager.isInStaatsFrak(players)) {
                                    players.sendMessage("§c§lHilfe! §3" + playerData.getFaction() + " " + player.getName() + " benötige Unterstützung! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                                    TextComponent start = new TextComponent("§8 ➥ ");
                                    TextComponent route = new TextComponent("§aRoute");
                                    TextComponent mid = new TextComponent("§8 | ");
                                    TextComponent toPlayer = new TextComponent("§aUnterwegs!");
                                    route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                                    route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));

                                    toPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reinforcement gotoreinf " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ() + " " + player.getName() + " dep"));
                                    toPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oIch bin unterwegs!")));

                                    players.spigot().sendMessage(start, route, mid, toPlayer);
                                    players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                                }
                            }
                        } else if (FactionManager.isInBündnis(player)) {
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                if (FactionManager.isInBündnisWith(players, playerData.getFaction())) {
                                    players.sendMessage("§c§lHilfe! §3" + playerData.getFaction() + " " + player.getName() + " benötige Unterstützung! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                                    TextComponent start = new TextComponent("§8 ➥ ");
                                    TextComponent route = new TextComponent("§aRoute");
                                    TextComponent mid = new TextComponent("§8 | ");
                                    TextComponent toPlayer = new TextComponent("§aUnterwegs!");
                                    route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                                    route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));

                                    toPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reinforcement gotoreinf " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ() + " " + player.getName() + " dep"));
                                    toPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oIch bin unterwegs!")));

                                    players.spigot().sendMessage(start, route, mid, toPlayer);
                                    players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                                }
                            }
                        }
                        break;
                    case "-m":
                        if (PlayerManager.isInStaatsFrak(player)) {
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                                if (PlayerManager.isInStaatsFrak(players)) {
                                    players.sendMessage("§c§lMediziner benötigt! §3" + playerData.getFaction() + " " + player.getName() + " benöge einen Mediziner! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                                    TextComponent start = new TextComponent("§8 ➥ ");
                                    TextComponent route = new TextComponent("§aRoute");
                                    TextComponent mid = new TextComponent("§8 | ");
                                    TextComponent toPlayer = new TextComponent("§aUnterwegs!");
                                    route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                                    route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));

                                    toPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reinforcement gotoreinf " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ() + " " + player.getName() + " dep"));
                                    toPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oIch bin unterwegs!")));

                                    players.spigot().sendMessage(start, route, mid, toPlayer);
                                    players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                                }
                            }
                        }
                        break;
                    case "gotoreinf":
                        if (args.length >= 3) {
                            Location loc = new Location(player.getWorld(), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                            String type = args[5];
                            if (!type.equals("dep")) {
                                for (Player players : Bukkit.getOnlinePlayers()) {
                                    PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                                    if (Objects.equals(playerData.getFaction(), playerData1.getFaction())) {
                                        players.sendMessage("§8 ➥ §b" + player.getName() + " §7➡ §3" + args[4] + " §8[§b" + (int) player.getLocation().distance(loc) + "m§8]");
                                    }
                                }
                            } else {
                                if (PlayerManager.isInStaatsFrak(player)) {
                                    for (Player players : Bukkit.getOnlinePlayers()) {
                                        players.sendMessage("§8 ➥ §b" + playerData.getFaction() + " " + player.getName() + " §7➡ §3" + args[4] + " §8[§b" + (int) player.getLocation().distance(loc) + "m§8]");
                                    }
                                } else if (FactionManager.isInBündnis(player)) {
                                    for (Player players : Bukkit.getOnlinePlayers()) {
                                        if (FactionManager.isInBündnisWith(players, playerData.getFaction())) {
                                            players.sendMessage("§8 ➥ §b" + playerData.getFaction() + " " + player.getName() + " §7➡ §3" + args[4] + " §8[§b" + (int) player.getLocation().distance(loc) + "m§8]");
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                            if (Objects.equals(playerData.getFaction(), playerData1.getFaction())) {
                                players.sendMessage("§c§lHilfe! §3" + player.getName() + " benötige Unterstützung! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                                TextComponent start = new TextComponent("§8 ➥ ");
                                TextComponent route = new TextComponent("§aRoute");
                                TextComponent mid = new TextComponent("§8 | ");
                                TextComponent toPlayer = new TextComponent("§aUnterwegs!");
                                route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                                route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));

                                toPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reinforcement gotoreinf " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ() + " " + player.getName() + " normal"));
                                toPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oIch bin unterwegs!")));

                                players.spigot().sendMessage(start, route, mid, toPlayer);
                                players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                            }
                        }
                        break;
                }
            }
        }
        return false;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("-d");
            suggestions.add("-p");
            suggestions.add("-e");
            suggestions.add("-ep");
            suggestions.add("-ed");
            suggestions.add("-m");

            return suggestions;
        }
        return null;
    }
}
