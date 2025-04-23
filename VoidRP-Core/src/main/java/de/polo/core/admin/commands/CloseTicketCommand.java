package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.manager.SupportManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.player.PlayerPacket;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CloseTicketCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final SupportManager supportManager;
    private final Utils utils;

    public CloseTicketCommand(PlayerManager playerManager, SupportManager supportManager, Utils utils) {
        this.playerManager = playerManager;
        this.supportManager = supportManager;
        this.utils = utils;
        Main.registerCommand("closesupport", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() >= 40) {
            Player targetplayer = null;
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (supportManager.getTicket(player).getCreator() == players.getUniqueId()) {
                    targetplayer = players;
                    Utils.Tablist.updatePlayer(players);
                }
                if (supportManager.getTicket(player).getEditors().contains(players.getUniqueId()) && player != players) {
                    players.sendMessage(Prefix.SUPPORT + "§aDas Ticket wurde von §2" + player.getName() + "§a geschlossen.");
                    Utils.Tablist.updatePlayer(players);
                }
            }
            if (!supportManager.deleteTicketConnection(player, targetplayer)) {
                player.sendMessage(Prefix.SUPPORT + "Du bearbeitest kein Ticket.");
                return false;
            }
            Utils.Tablist.updatePlayer(player);
            targetplayer.sendMessage(Prefix.SUPPORT + "§c" + playerManager.rang(player) + " " + player.getName() + " hat dein Ticket geschlossen!");
            utils.sendActionBar(targetplayer, "§c§lDein Ticket wurde geschlossen!");
            player.sendMessage(Prefix.SUPPORT + "§aDu hast das Ticket von §2" + targetplayer.getName() + "§a geschlossen.");
            PlayerPacket playerPacket = new PlayerPacket(player);
            playerPacket.renewPacket();
            PlayerPacket targetPacket = new PlayerPacket(targetplayer);
            targetPacket.renewPacket();

            AdminService adminService = VoidAPI.getService(AdminService.class);
            adminService.sendAdminMessage(player.getName() + " hat das Ticket von " + targetplayer.getName() + " geschlossen.", Color.YELLOW);
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }
}
