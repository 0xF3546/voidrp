package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.PlayerData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.PhoneUtils;
import de.polo.void_roleplay.Utils.PlayerManager;
import de.polo.void_roleplay.Utils.StaatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class serviceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
            if (playerData.getVariable("service") == null) {
                if (PhoneUtils.hasPhone(player)) {
                    if (!playerData.isFlightmode()) {
                        if (Integer.parseInt(args[0]) == 112 || Integer.parseInt(args[0]) == 110) {
                            StringBuilder msg = new StringBuilder(args[1]);
                            for (int i = 2; i < args.length; i++) {
                                msg.append(" ").append(args[i]);
                                player.sendMessage("§8[§6Notruf§8] §aService abgesendet!");
                            }
                            playerData.setVariable("service", "asd");
                            StaatUtil.createService(player, Integer.parseInt(args[0]), msg.toString());
                        } else {
                            player.sendMessage(Main.error + "Syntax-Fehler: /service [§l110/112§7] [Nachricht]");
                        }
                    } else {
                        player.sendMessage(PhoneUtils.error_flightmode);
                    }
                } else {
                    player.sendMessage(PhoneUtils.error_nophone);
                }
            } else {
                player.sendMessage(Main.error + "Du hast bereits einen Service offen.");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /service [110/112] [Nachricht]");
        }
        return false;
    }
}
