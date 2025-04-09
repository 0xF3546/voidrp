package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlyCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public FlyCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("fly", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 80) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
        if (!voidPlayer.isAduty()) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht im Admindienst!");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /fly [Spieler]");
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Prefix.ERROR + "Der Spieler wurde nicht gefunden.");
            return false;
        }
        AdminService adminService = VoidAPI.getService(AdminService.class);
        PlayerData targetData = playerManager.getPlayerData(target);
        if (target.getAllowFlight()) {
            target.setFlying(false);
            target.setAllowFlight(false);
            target.sendMessage(Prefix.ADMIN + player.getName() + " hat dir Fly entfernt.");
            adminService.send_message(player.getName() + " hat " + target.getName() + " Fly entfernt.", null);
        } else {
            target.setAllowFlight(true);
            target.setFlying(true);
            target.sendMessage(Prefix.ADMIN + player.getName() + " hat dir Fly gegeben.");
            adminService.send_message(player.getName() + " hat " + target.getName() + " Fly gegeben.", null);
        }
        Utils.Tablist.updatePlayer(target);
        return false;
    }
}
