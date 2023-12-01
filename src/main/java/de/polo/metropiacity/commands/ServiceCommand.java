package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.PhoneUtils;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.StaatUtil;
import de.polo.metropiacity.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ServiceCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;
    public ServiceCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("service", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1) {
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
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
                            utils.staatUtil.createService(player, Integer.parseInt(args[0]), msg.toString());
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
