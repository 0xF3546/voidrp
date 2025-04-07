package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.PhoneUtils;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SMSCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public SMSCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("sms", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (PhoneUtils.hasPhone(player)) {
            if (!playerData.isFlightmode()) {
                if (args.length >= 2) {
                    Player targetplayer = Bukkit.getPlayer(args[0]);
                    StringBuilder msg = new StringBuilder(args[1]);
                    for (int i = 2; i < args.length; i++) {
                        msg.append(' ').append(args[i]);
                    }
                    utils.phoneUtils.sendSMS(player, targetplayer, msg);
                } else {
                    player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /sms [Spieler] [Nachricht]");
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
