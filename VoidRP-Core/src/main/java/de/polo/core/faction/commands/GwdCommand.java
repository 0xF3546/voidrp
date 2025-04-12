package de.polo.core.faction.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class GwdCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public GwdCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
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
            AdminService adminService = VoidAPI.getService(AdminService.class);
            adminService.sendMessage(player.getName() + " hat " + target.getName() + " GWD-Note " + grade + " gegeben.", Color.NAVY);
            factionManager.sendCustomLeaderMessageToFactions("§3HQ: " + player.getName() + " hat " + target.getName() + " GWD-Note " + grade + " gegeben.", "Polizei");
            target.sendMessage("§6Dir wurde GWD-Note " + grade + " gegeben.");
            targetData.setGwd(grade);
            Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET gwd = ? WHERE uuid = ?", grade, target.getUniqueId().toString());
        } catch (Exception e) {
            player.sendMessage(Prefix.ERROR + "Die GWD-Note muss numerisch sein.");
        }
        return false;
    }
}
