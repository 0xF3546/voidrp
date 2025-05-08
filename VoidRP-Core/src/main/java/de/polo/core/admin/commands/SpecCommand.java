package de.polo.core.admin.commands;

import de.polo.api.Utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandBase.CommandMeta(
        name = "spec",
        usage = "/spec [Spieler]",
        adminDuty = true
)
public class SpecCommand extends CommandBase {

    public SpecCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        AdminService adminService = VoidAPI.getService(AdminService.class);
        boolean isSpec = playerData.getVariable("isSpec") != null;
        if (isSpec) {
            leaveSpec(player);
            return;
        }
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage("Der Spieler ist nicht online.", Prefix.ERROR);
            return;
        }
        player.setVariable("specLoc", player.getLocation());
        player.getPlayer().teleport(targetplayer.getLocation());
        player.getPlayer().setGameMode(GameMode.SPECTATOR);
        player.getPlayer().setSpectatorTarget(targetplayer);
        player.setVariable("isSpec", targetplayer.getUniqueId().toString());
        player.sendMessage(Prefix.ADMIN + "§cDu Spectatest nun §7" + targetplayer.getName() + "§c.");
        adminService.sendMessage(player.getName() + " beobachtet nun " + targetplayer.getName(), Color.RED);

    }

    public void leaveSpec(VoidPlayer player) {
        player.getPlayer().setGameMode(GameMode.SURVIVAL);
        player.getPlayer().setAllowFlight(true);
        player.getPlayer().teleport((Location) player.getVariable("specLoc"));
        player.setVariable("isSpec", null);
    }
}
