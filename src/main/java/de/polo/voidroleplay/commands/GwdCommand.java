package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GwdCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final FactionManager factionManager;

    public GwdCommand(PlayerManager playerManager, AdminManager adminManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.factionManager = factionManager;

        Main.registerCommand("gwd", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.getFaction().equalsIgnoreCase("Polizei")) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (playerData.getFactionGrade() < 4) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /gwd [Spieler] [Note]");
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Prefix.ERROR + "Spieler wurde nicht gefunden.");
            return false;
        }
        try {
            int grade = Integer.parseInt(args[1]);
            if (!(grade >= 1 && grade <= 15)) {
                player.sendMessage(Prefix.ERROR + "Die GWD-Note muss zwischen 1-15 sein.");
                return false;
            }
            PlayerData targetData = playerManager.getPlayerData(target);
            adminManager.send_message(player.getName() + " hat " + target.getName() + " GWD-Note " + grade + " gegeben.", ChatColor.DARK_AQUA);
            factionManager.sendCustomLeaderMessageToFactions("§3HQ: " + player.getName() + " hat " + target.getName() + " GWD-Note " + grade + " gegeben.", "Polizei");
            target.sendMessage("§6Dir wurde GWD-Note " + grade + " gegeben.");
            targetData.setGwd(grade);
            Main.getInstance().getMySQL().updateAsync("UPDATE players SET gwd = ? WHERE uuid = ?", grade, target.getUniqueId().toString());
        } catch (Exception e) {
            player.sendMessage(Prefix.ERROR + "Die GWD-Note muss numerisch sein.");
        }
        return false;
    }
}
