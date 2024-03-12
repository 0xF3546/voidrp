package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.dataStorage.BusinessData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.utils.BusinessManager;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BusinessChatCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final BusinessManager businessManager;
    public BusinessChatCommand(PlayerManager playerManager, BusinessManager businessManager) {
        this.playerManager = playerManager;
        this.businessManager = businessManager;
        Main.registerCommand("businesschat", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getBusiness() == null || playerData.getBusiness() == 0) {
            player.sendMessage(Main.business_prefix + "Du bist in keinem Business.");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /businesschat [Nachricht]");
            return false;
        }
        BusinessData businessData = businessManager.getBusinessData(playerData.getBusiness());
        if (!businessData.isActive()) {
            player.sendMessage(Main.error + "Dieses Business ist nicht aktiv.");
            return false;
        }
        StringBuilder msg = new StringBuilder(args[0]);
        for (int i = 1; i < args.length; i++) {
            msg.append(" ").append(args[i]);
        }
        for (Player players : Bukkit.getOnlinePlayers()) {
            PlayerData playersData = playerManager.getPlayerData(players.getUniqueId());
            if (playersData.getBusiness() != null) {
                if (playersData.getBusiness().equals(playerData.getBusiness())) {
                    players.sendMessage("§8[§6Business§8]§e " + player.getName() + "§8:§7 " + msg);
                }
            }
        }
        return false;
    }
}
