package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.storage.BusinessData;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.manager.BusinessManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.agreement.services.VertragUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BizInviteCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;
    private final BusinessManager businessManager;

    public BizInviteCommand(PlayerManager playerManager, Utils utils, BusinessManager businessManager) {
        this.playerManager = playerManager;
        this.utils = utils;
        this.businessManager = businessManager;
        Main.registerCommand("bizinvite", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getBusiness() == null || playerData.getBusiness() == 0) {
            player.sendMessage(Prefix.ERROR + "Du bist in keinem Business");
            return false;
        }
        if (!(playerData.getBusiness_grade() >= 4)) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (!(args.length >= 1)) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /bizinvite [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Prefix.ERROR + args[0] + " ist nicht online.");
            return false;
        }
        if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in deiner nähe.");
            return false;
        }
        if (playerManager.getPlayerData(targetplayer.getUniqueId()).getBusiness() != 0) {
            player.sendMessage("§8[§6Business§8] §c" + targetplayer.getName() + "§7 ist bereits in einem Business.");
            return false;
        }
        BusinessData businessData = businessManager.getBusinessData(playerData.getBusiness());
        if (BusinessManager.getMemberCount(playerData.getBusiness()) >= businessData.getMaxMember()) {
            player.sendMessage(Prefix.ERROR + "Dein Business ist voll!");
            return false;
        }
        if (VertragUtil.setVertrag(player, targetplayer, "business_invite", playerData.getBusiness())) {
            player.sendMessage("§8[§6Business§8] §7" + targetplayer.getName() + " wurde in das Business §aeingeladen§7.");
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(businessData.getOwner());
            targetplayer.sendMessage("§6" + player.getName() + " hat dich in das Business von §e" + offlinePlayer.getName() + "§6 eingeladen.");
            utils.vertragUtil.sendInfoMessage(targetplayer);
            PlayerData tplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        } else {
            player.sendMessage("§8[§6Business§8]§8 §7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
        }
        return false;
    }
}
