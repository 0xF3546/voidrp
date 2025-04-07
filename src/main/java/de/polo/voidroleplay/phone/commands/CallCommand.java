package de.polo.voidroleplay.phone.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.PhoneUtils;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import net.kyori.adventure.text.Component;
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
        if (!PhoneUtils.hasPhone(player)) {
            player.sendMessage(Component.text(PhoneUtils.ERROR_NO_PHONE));
            return false;
        }
        if (!playerData.isDead()) {
            if (!playerData.isFlightmode()) {
                if (args.length >= 1) {
                    if (!Objects.equals(playerData.getVariable("calling"), "Ja")) {
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            PlayerData targetplayerData = playerManager.getPlayerData(players.getUniqueId());
                            if (players.getName().equalsIgnoreCase(args[0])) {
                                if (utils.phoneUtils.getCall(players) == null) {
                                    if (!targetplayerData.isFlightmode()) {
                                        try {
                                            utils.phoneUtils.callNumber(player, players);
                                        } catch (SQLException e) {
                                            player.sendMessage(Prefix.ERROR + "Ein Fehler ist aufgetreten. Kontaktiere einen Entwickler.");
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        player.sendMessage(Prefix.ERROR + players.getName() + " ist nicht erreichbar.");
                                    }
                                } else {
                                    player.sendMessage(Prefix.ERROR + players.getName() + " ist bereits in einem Gespräch.");
                                }
                            }
                        }
                    } else {
                        player.sendMessage(Prefix.ERROR + "Du rufst bereits jemanden an.");
                    }
                } else {
                    player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /call [Spieler]");
                }
            } else {
                player.sendMessage(PhoneUtils.ERROR_FLIGHTMODE);
            }
        } else {
            player.sendMessage(PhoneUtils.ERROR_NO_PHONE);
        }
        return false;
    }
}
