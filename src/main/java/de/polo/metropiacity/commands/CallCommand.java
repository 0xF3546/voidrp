package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PhoneUtils;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import okhttp3.internal.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Objects;

public class CallCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;
    public CallCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("call", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!playerData.isDead()) {
            if (!playerData.isFlightmode()) {
                if (args.length >= 1) {
                        if (!Objects.equals(playerData.getVariable("calling"), "Ja")) {
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                PlayerData targetplayerData = playerManager.getPlayerData(players.getUniqueId());
                                if (players.getName().equalsIgnoreCase(args[0])) {
                                    if (PhoneUtils.getConnection(players) == null) {
                                        if (!targetplayerData.isFlightmode()) {
                                            try {
                                                utils.phoneUtils.callNumber(player, players);
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
                    player.sendMessage(Main.error + "Syntax-Fehler: /call [Spieler]");
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
