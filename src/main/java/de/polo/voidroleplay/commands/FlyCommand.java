package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlyCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;

    public FlyCommand(PlayerManager playerManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        Main.registerCommand("fly", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getPermlevel() < 80) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (!playerData.isAduty()) {
            player.sendMessage(Main.error + "Du bist nicht im Admindienst!");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /fly [Spieler]");
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Main.error + "Der Spieler wurde nicht gefunden.");
            return false;
        }
        PlayerData targetData = playerManager.getPlayerData(target);
        if (target.getAllowFlight()) {
            target.setFlying(false);
            target.setAllowFlight(false);
            target.sendMessage(Prefix.ADMIN + player.getName() + " hat dir Fly entfernt.");
            adminManager.send_message(player.getName() + " hat " + target.getName() + " Fly entfernt.", null);
        } else {
            target.setAllowFlight(true);
            target.setFlying(true);
            target.sendMessage(Prefix.ADMIN + player.getName() + " hat dir Fly gegeben.");
            adminManager.send_message(player.getName() + " hat " + target.getName() + " Fly gegeben.", null);
        }
        Utils.Tablist.updatePlayer(target);
        return false;
    }
}
