package de.polo.core.faction.commands;

import de.polo.core.Main;
import de.polo.core.handler.TabCompletion;
import de.polo.core.faction.entity.Faction;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.admin.services.impl.AdminManager;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static de.polo.core.Main.adminService;

public class LeadFrakCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final AdminManager adminManager;
    private final FactionManager factionManager;

    public LeadFrakCommand(PlayerManager playerManager, AdminManager adminManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.adminManager = adminManager;
        this.factionManager = factionManager;
        Main.registerCommand("leadfrak", this);
        Main.addTabCompleter("leadfrak", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() < 75) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /leadfrak [Spieler] [Fraktion]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        String frak = args[1];
        player.sendMessage(Prefix.ADMIN + "Du hast §c" + targetplayer.getName() + "§7 in die Fraktion §c" + frak + "§7 gesetzt.");
        targetplayer.sendMessage(Prefix.FACTION + "Du bist nun Leader der Fraktion §c" + frak + "§7!");
        factionManager.setPlayerInFrak(targetplayer, frak, 6);
        factionManager.setLeader(targetplayer, true);
        adminService.send_message(player.getName() + " hat " + targetplayer.getName() + " in die Fraktion " + frak + " gesetzt.", Color.PURPLE);
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 2) {
            List<String> factions = new ObjectArrayList<>();
            for (Faction factionData : factionManager.getFactions()) {
                factions.add(factionData.getName());
            }
            return factions;
        }
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, Bukkit.getOnlinePlayers()
                        .stream()
                        .map(Player::getName)
                        .toList())
                .addAtIndex(2, factionManager.getFactions()
                        .stream()
                        .map(Faction::getName)
                        .toList())
                .build();
    }
}
