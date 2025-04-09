package de.polo.core.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ZDCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public ZDCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;

        Main.registerCommand("zd", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.getFaction().equalsIgnoreCase("Medic")) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (playerData.getFactionGrade() < 4) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /zd [Spieler] [Note]");
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
                player.sendMessage(Prefix.ERROR + "Die ZD-Note muss zwischen 1-15 sein.");
                return false;
            }
            AdminService adminService = VoidAPI.getService(AdminService.class);
            PlayerData targetData = playerManager.getPlayerData(target);
            adminService.send_message(player.getName() + " hat " + target.getName() + " ZD-Note " + grade + " gegeben.", Color.PURPLE);
            factionManager.sendCustomLeaderMessageToFactions("§cHQ: " + player.getName() + " hat " + target.getName() + " ZD-Note " + grade + " gegeben.", "Medic");
            target.sendMessage("§6Dir wurde ZD-Note " + grade + " gegeben.");
            targetData.setZd(grade);
            Main.getInstance().getCoreDatabase().updateAsync("UPDATE players SET zd = ? WHERE uuid = ?", grade, target.getUniqueId().toString());
        } catch (Exception e) {
            player.sendMessage(Prefix.ERROR + "Die ZD-Note muss numerisch sein.");
        }
        return false;
    }
}
