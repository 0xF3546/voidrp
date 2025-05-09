package de.polo.core.faction.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.admin.services.AdminService;
import de.polo.core.faction.entity.Faction;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.handler.TabCompletion;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SetFrakCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public SetFrakCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.registerCommand("setfrak", this);
        Main.addTabCompleter("setfrak", this);
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
                if (rang >= 0 && rang <= 8) {
                    AdminService adminService = VoidAPI.getService(AdminService.class);
                    factionManager.setPlayerInFrak(targetplayer, frak, rang);
                    adminService.sendMessage(player.getName() + " hat " + targetplayer.getName() + " in die Fraktion " + frak + " (Rang " + rang + ") gesetzt.", Color.PURPLE);
                } else {
                    player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /setfraktion [Spieler] [Fraktion] [Rang(1-8)]");
                    return false;
                }
                player.sendMessage(Prefix.ADMIN + "Du hast §c" + targetplayer.getName() + "§7 in die Fraktion §c" + frak + "§7 (Rang §c" + rang + "§7) gesetzt.");
                targetplayer.sendMessage(Prefix.FACTION + "Du bist Rang §c" + rang + "§7 der Fraktion §c" + frak + "§7!");
            } else {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /setfrak [Spieler] [Fraktion] [Rang]");
            }
        } else {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, Bukkit.getOnlinePlayers()
                        .stream()
                        .map(Player::getName)
                        .toList())
                .addAtIndex(2, factionManager.getFactions()
                        .stream()
                        .map(Faction::getName)
                        .toList())
                .addAtIndex(3, List.of("0", "1", "2", "3", "4", "5", "6"))
                .build();
    }
}
