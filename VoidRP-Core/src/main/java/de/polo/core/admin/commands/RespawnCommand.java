package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.location.services.LocationService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RespawnCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public RespawnCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
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
        VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
        if (!voidPlayer.isAduty()) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht im Admindienst!");
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
        AdminService adminService = VoidAPI.getService(AdminService.class);
        adminService.sendMessage(player.getName() + " hat " + targetplayer.getName() + " respawnt.", null);
        PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (targetplayerData.getFaction() != null) {
            locationService.useLocation(targetplayer, targetplayerData.getFaction());
        } else {
            locationService.useLocation(targetplayer, "Stadthalle");
        }
        utils.deathUtil.revivePlayer(targetplayer, false);
        return false;
    }
}
