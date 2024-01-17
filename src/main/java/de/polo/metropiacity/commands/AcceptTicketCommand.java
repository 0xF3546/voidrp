package de.polo.metropiacity.commands;

import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.Ticket;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.AdminManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.SupportManager;
import de.polo.metropiacity.utils.Utils;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Statement;

public class AcceptTicketCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final SupportManager supportManager;
    private final Utils utils;
    private final MySQL mySQL;

    public AcceptTicketCommand(PlayerManager playerManager, AdminManager adminManager, SupportManager supportManager, Utils utils, MySQL mySQL) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.supportManager = supportManager;
        this.utils = utils;
        this.mySQL = mySQL;
        Main.registerCommand("acceptsupport", this);
    }

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 40) {
            player.sendMessage(Main.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Main.support_prefix + "Syntax-Fehler: /acceptsupport [Spieler]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null) {
            player.sendMessage(Main.support_prefix + "§c" + args[0] + "§7 ist §cnicht §7online.");
            return false;
        }
        if (supportManager.isInConnection(player)) {
            player.sendMessage(Main.error + "Du bearbeitest bereits ein Ticket.");
            return false;
        }
        if (!supportManager.ticketCreated(targetplayer)) {
            player.sendMessage(Main.support_prefix + "§c" + targetplayer.getName() + "§7 hat kein Ticket erstellt.");
            return false;
        }
        supportManager.createTicketConnection(targetplayer, player);
        targetplayer.sendMessage(Main.support_prefix + "§c" + playerManager.rang(player) + " " + player.getName() + "§7 bearbeitet nun dein Ticket!");
        player.sendMessage(Main.support_prefix + "Du bearbeitest nun das Ticket von §c" + targetplayer.getName() + "§7.");
        adminManager.send_message(player.getName() + " bearbeitet nun das Ticket von " + targetplayer.getName() + ".", ChatColor.YELLOW);
        utils.sendActionBar(targetplayer, "§a§lDein Ticket wurde angenommen!");

        Ticket ticket = supportManager.getTicket(player);
        Statement statement = mySQL.getStatement();
        statement.execute("UPDATE tickets SET editor = '" + player.getUniqueId() + "', editedAt = NOW() WHERE id = " + ticket.getId());
        statement.close();
        return false;
    }
}
