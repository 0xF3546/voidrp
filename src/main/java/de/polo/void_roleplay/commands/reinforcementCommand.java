package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class reinforcementCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
            if (args.length == 0) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                    if (Objects.equals(playerData.getFaction(), playerData1.getFaction())) {
                        players.sendMessage("§8[§c§lReinforcement§8]§c " + player.getName()+ " braucht Unterstützung! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                        TextComponent route = new TextComponent("§8 ➥ §aRoute");
                        route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                        route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));
                        players.spigot().sendMessage(route);
                        players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                    }
                }
            } else {
                switch (args[0]) {
                    case "-d":
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                            if (Objects.equals(playerData.getFaction(), playerData1.getFaction())) {
                                players.sendMessage("§8[§c§lReinforcement§8] §4§lDRINGEND! §8| §c" + player.getName()+ " braucht Unterstützung! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                                TextComponent route = new TextComponent("§8 ➥ §aRoute");
                                route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                                route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));
                                players.spigot().sendMessage(route);
                            }
                        }
                        break;
                    case "-p":
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                            if (Objects.equals(playerData.getFaction(), playerData1.getFaction())) {
                                players.sendMessage("§8[§c§lReinforcement§8] §eGPS §8| §c" + player.getName()+ " sendet seine GPS-Daten![" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                                TextComponent route = new TextComponent("§8 ➥ §aRoute");
                                route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                                route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));
                                players.spigot().sendMessage(route);
                            }
                        }
                        break;
                    default:
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            PlayerData playerData1 = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                            if (Objects.equals(playerData.getFaction(), playerData1.getFaction())) {
                                players.sendMessage("§8[§c§lReinforcement§8]§c " + player.getName()+ " braucht Unterstützung![" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                                TextComponent route = new TextComponent("§8 ➥ §aRoute");
                                route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                                route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));
                                players.spigot().sendMessage(route);
                            }
                        }
                        break;
                }
            }
        }
        return false;
    }
}
