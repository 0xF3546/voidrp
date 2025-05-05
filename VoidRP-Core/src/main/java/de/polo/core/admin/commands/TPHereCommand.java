package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandBase.CommandMeta(
        name = "tphere",
        usage = "/tphere [Spieler]",
        adminDuty = true,
        permissionLevel = 70
)
public class TPHereCommand extends CommandBase {

    public TPHereCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (!targetplayer.isOnline()) {
            player.sendMessage(Prefix.ERROR + args[0] + " ist nicht online.");
            return;
        }
        targetplayer.teleport(player.getLocation());
        player.sendMessage(Prefix.ADMIN + "Du hast §c" + targetplayer.getName() + "§7 zu dir teleportiert.");
        targetplayer.sendMessage(Prefix.MAIN + "§c" + playerData.getRang() + " " + player.getName() + "§7 hat dich zu sich teleportiert.");
        AdminService adminService = VoidAPI.getService(AdminService.class);
        adminService.sendMessage(player.getName() + " hat " + targetplayer.getName() + " zu sich teleportiert.", Color.RED);
    }
}
