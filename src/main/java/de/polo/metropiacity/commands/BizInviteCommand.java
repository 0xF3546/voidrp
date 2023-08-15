package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.BusinessData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.utils.BusinessManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.VertragUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class BizInviteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getBusiness() == null) {
            player.sendMessage(Main.error + "Du bist in keinem Business");
            return false;
        }
        if (!(playerData.getBusiness_grade() >= 4)) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (!(args.length >= 1)) {
            player.sendMessage(Main.error + "Syntax-Fehler: /bizinvite [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Main.error + args[0] + " ist nicht online.");
            return false;
        }
        if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
            player.sendMessage(Main.error + targetplayer.getName() + " ist nicht in deiner nähe.");
            return false;
        }
        if (PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString()).getBusiness() != null) {
            player.sendMessage("§8[§6Business§8] §c" + targetplayer.getName() + "§7 ist bereits in einem Business.");
            return false;
        }
        BusinessData businessData = BusinessManager.businessDataMap.get(playerData.getBusiness());
        if (BusinessManager.getMemberCount(playerData.getBusiness()) >= businessData.getMaxMember()) {
            player.sendMessage(Main.error + "Dein Business ist voll!");
            return false;
        }
        if (VertragUtil.setVertrag(player, targetplayer, "business_invite", playerData.getBusiness())) {
            player.sendMessage("§8[§6Business§8] §7" + targetplayer.getName() + " wurde in das Business §aeingeladen§7.");
            targetplayer.sendMessage("§6" + player.getName() + " hat dich in das Business §e" + playerData.getBusiness() + "§6 eingeladen.");
            VertragUtil.sendInfoMessage(targetplayer);
            PlayerData tplayerData = PlayerManager.playerDataMap.get(targetplayer.getUniqueId().toString());
        } else {
            player.sendMessage("§8[§6Business§8]§8 §7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
        }
        return false;
    }
}
