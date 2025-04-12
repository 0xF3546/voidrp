package de.polo.core.faction.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.handler.TabCompletion;
import de.polo.core.faction.entity.Faction;
import de.polo.core.location.services.NavigationService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.PhoneUtils;
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

import java.util.List;
import java.util.Objects;

public class ReinforcementCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public ReinforcementCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("reinforcement", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!PhoneUtils.hasPhone(player)) {
            player.sendMessage(PhoneUtils.ERROR_NO_PHONE);
            return false;
        }
        if (playerData.isFlightmode()) {
            player.sendMessage(PhoneUtils.ERROR_FLIGHTMODE);
            return false;
        }
        if (playerData.getFaction() != null && !Objects.equals(playerData.getFaction(), "Zivilist")) {
            if (args.length == 0) {
                sendReinforcement(player, "§cHilfe!", false);
            } else {
                switch (args[0]) {
                    case "-d":
                        sendReinforcement(player, "§cHilfe!", true);
                        break;
                    case "-p":
                        sendReinforcement(player, "§eGPS!", false);
                        break;
                    case "-ed":
                        sendReinforcement(player, "§4Dringend!", true);
                        break;
                    case "-ep":
                        sendReinforcement(player, "§eGPS!", true);
                        break;
                    case "-e":
                        sendReinforcement(player, "§4Dringend!", false);
                        break;
                    case "-m":
                        sendReinforcement(player, "§cMedic!", true);
                        break;
                    case "-lb":
                        sendReinforcement(player, "§cLeichenbewachung!", true);
                    case "gotoreinf":
                        if (args.length >= 3) {
                            Location loc = new Location(player.getWorld(), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                            String type = args[5];
                            NavigationService navigationService = VoidAPI.getService(NavigationService.class);
                            navigationService.createNaviByCord(player, (int) loc.getX(), (int) loc.getY(), (int) loc.getZ());
                            if (!type.equals("dep")) {
                                for (Player players : Bukkit.getOnlinePlayers()) {
                                    PlayerData playerData1 = playerManager.getPlayerData(players.getUniqueId());
                                    if (Objects.equals(playerData.getFaction(), playerData1.getFaction())) {
                                        players.sendMessage("§8 » §b" + player.getName() + " §7➡ §b" + args[4] + " §8[§b" + (int) player.getLocation().distance(loc) + "m§8]");
                                    }
                                }
                            } else {
                                if (playerManager.isInStaatsFrak(player)) {
                                    for (Player players : Bukkit.getOnlinePlayers()) {
                                        if (playerManager.isInStaatsFrak(players)) {
                                            players.sendMessage("§8 » §b" + playerData.getFaction() + " " + player.getName() + " §7➡ §b" + args[4] + " §8[§b" + (int) player.getLocation().distance(loc) + "m§8]");
                                        }
                                    }
                                } else if (factionManager.isInBündnis(player)) {
                                    for (Player players : Bukkit.getOnlinePlayers()) {
                                        if (factionManager.isInBündnisWith(players, playerData.getFaction())) {
                                            players.sendMessage("§8 » §b" + playerData.getFaction() + " " + player.getName() + " §7➡ §b" + args[4] + " §8[§b" + (int) player.getLocation().distance(loc) + "m§8]");
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        sendReinforcement(player, "§cHilfe!", false);
                        break;
                }
            }
        }
        return false;
    }

    private void sendReinforcement(Player player, String variation, boolean isBündnis) {
        PlayerData pData = playerManager.getPlayerData(player);
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = playerManager.getPlayerData(players);
            if (playerData.getFaction() == null) continue;
            if (isBündnis) {
                if (playerManager.isInStaatsFrak(player)) {
                    if (playerManager.isInStaatsFrak(players)) {
                        players.sendMessage(variation + " §b" + pData.getFaction() + " " + player.getName() + " benötige Unterstützung! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                        TextComponent start = new TextComponent("§8 » ");
                        TextComponent route = new TextComponent("§cRoute Anzeigen");
                        TextComponent mid = new TextComponent("§8 | ");
                        TextComponent toPlayer = new TextComponent("§aUnterwegs!");
                        route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                        route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));

                        toPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reinforcement gotoreinf " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ() + " " + player.getName() + " dep"));
                        toPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oIch bin unterwegs!")));

                        players.spigot().sendMessage(start, route, mid, toPlayer);
                        players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                        continue;
                    }
                }
                Faction alliance = Main.getInstance().gamePlay.alliance.getAlliance(pData.getFaction());
                if (alliance == null) continue;
                System.out.println(alliance.getName());
                if (playerData.getFaction().equalsIgnoreCase(alliance.getName()) || playerData.getFaction().equalsIgnoreCase(pData.getFaction())) {
                    players.sendMessage(variation + " §b" + pData.getFaction() + " " + player.getName() + " benötige Unterstützung! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                    TextComponent start = new TextComponent("§8 » ");
                    TextComponent route = new TextComponent("§cRoute Anzeigen");
                    TextComponent mid = new TextComponent("§8 | ");
                    TextComponent toPlayer = new TextComponent("§aUnterwegs!");
                    route.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/navi " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ()));
                    route.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oRoute verfolgen")));

                    toPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reinforcement gotoreinf " + (int) player.getLocation().getX() + " " + (int) player.getLocation().getY() + " " + (int) player.getLocation().getZ() + " " + player.getName() + " dep"));
                    toPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oIch bin unterwegs!")));

                    players.spigot().sendMessage(start, route, mid, toPlayer);
                    players.playSound(players.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                    continue;
                }
            } else {
                if (playerData.getFaction().equalsIgnoreCase(pData.getFaction())) {
                    players.sendMessage(variation + " §b" + player.getName() + " benötige Unterstützung! [" + (int) player.getLocation().distance(players.getLocation()) + "m]");
                    TextComponent start = new TextComponent("§8 » ");
                    TextComponent route = new TextComponent("§cRoute Anzeigen");
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
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, List.of("-d", "-p", "-e", "-ep", "-ed", "-m", "-lb"))
                .build();
    }
}
