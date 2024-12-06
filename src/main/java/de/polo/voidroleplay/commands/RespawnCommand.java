package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RespawnCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final Utils utils;
    private final LocationManager locationManager;

    public RespawnCommand(PlayerManager playerManager, AdminManager adminManager, Utils utils, LocationManager locationManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.utils = utils;
        this.locationManager = locationManager;
        Main.registerCommand("respawn", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 60) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (!playerData.isAduty()) {
            player.sendMessage(Prefix.admin_error + "Du bist nicht im Admindienst!");
            return false;
        }
        if (!(args.length >= 1)) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /respawn [Spieler]");
            return false;
        } else {
            OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
            if (offlinePlayer == null) {
                player.sendMessage(Prefix.ERROR + args[0] + " wurde nicht gefunden.");
                return false;
            }
            if (!offlinePlayer.isOnline()) {
                player.sendMessage(Prefix.ERROR + offlinePlayer.getName() + " ist nicht online.");
                return false;
            }
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        targetplayer.sendMessage(Prefix.MAIN + "§a" + player.getName() + " hat dich Respawnt!");
        adminManager.send_message(player.getName() + " hat " + targetplayer.getName() + " respawnt.", null);
        PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        if (targetplayerData.getFaction() != null) {
            locationManager.useLocation(targetplayer, targetplayerData.getFaction());
        } else {
            locationManager.useLocation(targetplayer, "Stadthalle");
        }
        utils.deathUtil.revivePlayer(targetplayer, false);
        return false;
    }
}
