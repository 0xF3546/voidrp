package de.polo.metropiacity.commands;

import de.polo.metropiacity.Main;
import de.polo.metropiacity.Utils.FactionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Objects;

public class uninviteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (FactionManager.faction_grade(player) >= 7) {
            if (args.length >= 1) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                System.out.println(targetplayer);
                String playerfac = FactionManager.faction(player);
                if (targetplayer != null && targetplayer.isOnline()) {
                    if (Objects.equals(FactionManager.faction(player), FactionManager.faction(targetplayer))) {
                        if (FactionManager.faction_grade(player) > FactionManager.faction_grade(targetplayer)) {
                            if (args.length >= 2) {
                            if (args[1].equalsIgnoreCase("confirm")) {
                                try {
                                    FactionManager.removePlayerFromFrak(targetplayer);
                                    targetplayer.sendMessage(Main.prefix + "Du wurdest von §c" + player.getName() + "§7 aus der Fraktion §c" + FactionManager.faction(player) + "§7 geworfen.");
                                    for (Player players : Bukkit.getOnlinePlayers()) {
                                        if (Objects.equals(FactionManager.faction(players), FactionManager.faction(player))) {
                                            players.sendMessage("§" + FactionManager.getFactionPrimaryColor(playerfac) + FactionManager.getFactionFullname(playerfac) + "§8 » §c" + targetplayer.getName() + "§7 wurde von §c" + player.getName() + "§7 aus der Fraktion geworfen.");
                                        }
                                    }
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                player.sendMessage(Main.error + "Syntax-Fehler: /uninvite " + args[0] + " confirm");
                            }
                            } else {
                                player.sendMessage(Main.error + "Syntax-Fehler: /uninvite " + targetplayer.getName() + " confirm");
                            }
                        } else {
                            player.sendMessage(Main.error_nopermission);
                        }
                    } else {
                        player.sendMessage(Main.faction_prefix + "Du und " + targetplayer.getName() + " seid nicht in der gleichen Fraktion.");
                    }
                } else {
                    try {
                        if (FactionManager.faction(player).equals(FactionManager.faction_offlinePlayer(args[0]))) {
                            if (FactionManager.faction_grade(player) > FactionManager.faction_grade_offlinePlayer(args[0])) {
                                if (args.length >= 2) {
                                if (args[1].equalsIgnoreCase("confirm")) {
                                    for (Player players : Bukkit.getOnlinePlayers()) {
                                        FactionManager.removeOfflinePlayerFromFrak(args[0]);
                                        if (Objects.equals(FactionManager.faction(players), FactionManager.faction(player))) {
                                            players.sendMessage("§" + FactionManager.getFactionPrimaryColor(playerfac) + FactionManager.getFactionFullname(playerfac) + "§8 » §c" + args[0] + "§7 wurde von §c" + player.getName() + "§7 aus der Fraktion geworfen.");
                                        }
                                    }
                                } else {
                                    player.sendMessage(Main.error + "Syntax-Fehler: /uninvite " + args[0] + " confirm");
                                }
                                } else {
                                    player.sendMessage(Main.error + "Syntax-Fehler: /uninvite " + args[0] + " confirm");
                                }
                            } else {
                                player.sendMessage(Main.error_nopermission);
                            }
                        } else {
                            player.sendMessage(Main.faction_prefix + "Du und " + args[0] + " seid nicht in der gleichen Fraktion.");
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                player.sendMessage(Main.faction_prefix + "Syntax-Fehler: /uninvite [Spieler] confirm");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
}
