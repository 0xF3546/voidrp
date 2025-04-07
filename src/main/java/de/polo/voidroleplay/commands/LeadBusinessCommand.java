package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.BusinessManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeadBusinessCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final BusinessManager businessManager;

    public LeadBusinessCommand(PlayerManager playerManager, BusinessManager businessManager) {
        this.playerManager = playerManager;
        this.businessManager = businessManager;
        Main.registerCommand("leadbusiness", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        /*if (playerData.getPermlevel() < 75) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Prefix.ADMIN_ERROR + "Syntax-Fehler: /leadbusiness [Spieler] [Business]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Prefix.ADMIN_ERROR + "Spieler nicht gefunden");
            return false;
        }
        String frak = args[1];
        try {
            businessManager.setPlayerInBusiness(targetplayer, frak, 8);
            PlayerData targetPlayerData = playerManager.getPlayerData(targetplayer);
            targetPlayerData.setBusiness(Integer.parseInt(args[1]));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        player.sendMessage(Main.admin_prefix + "Du hast §c" + targetplayer.getName() + "§7 in das Business §c" + frak + "§7 gesetzt.");
        targetplayer.sendMessage(Main.business_prefix + "Du bist nun Leader des Business §c" + frak + "§7!");*/
        return false;
    }
}
