package de.polo.core.faction.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class SetRankPayDayCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public SetRankPayDayCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("setrankpayday", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!(playerData.isLeader())) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (!(args.length >= 2)) {
            player.sendMessage(Prefix.FACTION + "Syntax-Fehler: /setrankpayday [Rang] [Gehalt]");
            return false;
        }
        if (Integer.parseInt(args[1]) < 0) {
            player.sendMessage(Prefix.ERROR + "Der PayDay muss größer gleich 0 sein!");
            return false;
        }
        if (Integer.parseInt(args[1]) > 2000) {
            player.sendMessage(Prefix.ERROR + "Der PayDay darf nicht größer als 2.000$ sein!");
            return false;
        }
        try {
            if (factionManager.changeRankPayDay(playerData.getFaction(), Integer.parseInt(args[0]), Integer.parseInt(args[1]))) {
                player.sendMessage(Prefix.FACTION + "PayDay von Rang §l" + args[0] + "§7 zu §l" + args[1] + "§§7 geändert.");

                AdminService adminService = VoidAPI.getService(AdminService.class);
                adminService.sendMessage(player.getName() + " den PayDay von Rang " + args[0] + " auf " + args[1] + "$ gesetzt (" + playerData.getFaction() + ").", Color.PURPLE);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
