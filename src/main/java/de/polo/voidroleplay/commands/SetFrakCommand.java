package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.utils.AdminManager;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SetFrakCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final FactionManager factionManager;
    public SetFrakCommand(PlayerManager playerManager, AdminManager adminManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.factionManager = factionManager;
        Main.registerCommand("setfrak", this);
        Main.addTabCompeter("setfrak", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String playerGroup = playerManager.rang(player);
        if (playerGroup.equalsIgnoreCase("Administrator") || playerGroup.equalsIgnoreCase("Fraktionsmanager")) {
            if (args.length >= 3) {
                Player targetplayer = Bukkit.getPlayer(args[0]);
                String frak = args[1];
                int rang = Integer.parseInt(args[2]);
                try {
                    if (rang >= 0 && rang <= 8) {
                        factionManager.setPlayerInFrak(targetplayer, frak, rang);
                        adminManager.send_message(player.getName() + " hat " + targetplayer.getName() + " in die Fraktion " + frak + " (Rang " + rang + ") gesetzt.", ChatColor.DARK_PURPLE);
                    } else {
                        player.sendMessage(Main.admin_error + "Syntax-Fehler: /setfraktion [Spieler] [Fraktion] [Rang(1-8)]");
                        return false;
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                player.sendMessage(Main.admin_prefix + "Du hast §c" + targetplayer.getName() + "§7 in die Fraktion §c" + frak + "§7 (Rang §c" + rang + "§7) gesetzt.");
                targetplayer.sendMessage(Main.faction_prefix + "Du bist Rang §c" + rang + "§7 der Fraktion §c" + frak + "§7!");
            } else {
                player.sendMessage(Main.admin_error + "Syntax-Fehler: /setfrak [Spieler] [Fraktion] [Rang]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 2) {
            List<String> factions = new ArrayList<>();
            for (FactionData factionData : factionManager.getFactions()) {
                factions.add(factionData.getName());
            }
            return factions;
        }
        if (args.length == 3) {
            List<String> grades = new ArrayList<>();
            for (int i = 0; i <= 8; i++) {
                grades.add(Integer.toString(i));
            }
            return grades;
        }
        return null;
    }
}
