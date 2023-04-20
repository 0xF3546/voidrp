package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PhoneUtils;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Objects;

public class callCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (PhoneUtils.hasPhone(player)) {
            if (!playerData.isFlightmode()) {
                if (args.length >= 1) {
                    if (!Objects.equals(playerData.getVariable("calling"), "Ja")) {
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            PlayerData targetplayerData = PlayerManager.playerDataMap.get(players.getUniqueId().toString());
                            if (targetplayerData.getNumber() == Integer.parseInt(args[0])) {
                                if (PhoneUtils.getConnection(players) == null) {
                                    if (!targetplayerData.isFlightmode()) {
                                        try {
                                            PhoneUtils.callNumber(player, targetplayerData.getNumber());
                                        } catch (SQLException e) {
                                            player.sendMessage(Main.error + "Ein Fehler ist aufgetreten. Kontaktiere einen Entwickler.");
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        player.sendMessage(Main.error + players.getName() + " ist nicht erreichbar.");
                                    }
                                } else {
                                    player.sendMessage(Main.error + players.getName() + " ist bereits in einem Gespräch.");
                                }
                            }
                        }
                    } else {
                        player.sendMessage(Main.error + "Du rufst bereits jemanden an.");
                    }
                } else {
                    player.sendMessage(Main.error + "Syntax-Fehler: /call [Nummer]");
                }
            } else {
                player.sendMessage(PhoneUtils.error_flightmode);
            }
        } else {
            player.sendMessage(PhoneUtils.error_nophone);
        }
        return false;
    }
}
