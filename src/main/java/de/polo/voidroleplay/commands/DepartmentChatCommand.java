package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DepartmentChatCommand implements CommandExecutor {
    private PlayerManager playerManager;
    public DepartmentChatCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("departmentchat", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerManager.isInStaatsFrak(player)) {
            if (args.length >= 1) {
                StringBuilder msg = new StringBuilder(args[0]);
                for (int i = 1; i < args.length; i++) {
                    msg.append(" ").append(args[i]);
                }
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (playerManager.getPlayerData(player).getFaction() != null) {
                        if (playerManager.isInStaatsFrak(players)) {
                            players.sendMessage("§c" + playerData.getFaction() + " " + player.getName() + "§8:§7 " + msg);
                        }
                    }
                }
            } else {
                player.sendMessage(Main.error + "Syntax-Fehler: /departmentchat [Nachricht]");
            }
        }
        return false;
    }
}
