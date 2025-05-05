package de.polo.core.admin.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.Ticket;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.manager.SupportManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.player.PlayerPacket;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static de.polo.core.Main.database;

public class AcceptTicketCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final SupportManager supportManager;
    private final Utils utils;

    public AcceptTicketCommand(PlayerManager playerManager, SupportManager supportManager, Utils utils) {
        this.playerManager = playerManager;
        this.supportManager = supportManager;
        this.utils = utils;
        Main.registerCommand("acceptsupport", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 40) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.SUPPORT + "Syntax-Fehler: /acceptsupport [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Prefix.SUPPORT + "§c" + args[0] + "§7 ist §cnicht §7online.");
            return false;
        }
        if (supportManager.isInConnection(player)) {
            player.sendMessage(Prefix.ERROR + "Du bearbeitest bereits ein Ticket.");
            return false;
        }
        if (!supportManager.ticketCreated(targetplayer)) {
            player.sendMessage(Prefix.SUPPORT + "§c" + targetplayer.getName() + "§7 hat kein Ticket erstellt.");
            return false;
        }
        AdminService adminService = VoidAPI.getService(AdminService.class);
        supportManager.createTicketConnection(targetplayer, player);
        targetplayer.sendMessage(Prefix.SUPPORT + "§c" + playerManager.rang(player) + " " + player.getName() + "§7 bearbeitet nun dein Ticket!");
        player.sendMessage(Prefix.SUPPORT + "Du bearbeitest nun das Ticket von §c" + targetplayer.getName() + "§7.");
        adminService.sendGuideMessage(player.getName() + " bearbeitet nun das Ticket von " + targetplayer.getName() + ".", Color.YELLOW);
        utils.sendActionBar(targetplayer, "§a§lDein Ticket wurde angenommen!");
        Utils.Tablist.setTablist(player, "§8[§6R§8]");
        Utils.Tablist.setTablist(targetplayer, "§8[§6R§8]");
        PlayerPacket playerPacket = new PlayerPacket(player);
        playerPacket.renewPacket();
        PlayerPacket targetPacket = new PlayerPacket(targetplayer);
        targetPacket.renewPacket();
        Ticket ticket = supportManager.getTicket(player);

        database.updateAsync("UPDATE tickets SET editor = ?. editedAt = NOW() WHERE id = ?", player.getUniqueId().toString(), ticket.getId());
        return false;
    }
}
