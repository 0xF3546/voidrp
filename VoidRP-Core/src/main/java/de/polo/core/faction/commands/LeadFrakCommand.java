package de.polo.core.faction.commands;

import de.polo.api.VoidAPI;
import de.polo.api.foundation.Constants;
import de.polo.api.player.VoidPlayer;
import de.polo.core.admin.services.AdminService;
import de.polo.core.faction.entity.Faction;
import de.polo.core.faction.service.FactionService;
import de.polo.core.handler.CommandBase;
import de.polo.core.handler.TabCompletion;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static de.polo.core.Main.factionManager;

@CommandBase.CommandMeta(
        name = "leadfrak",
        usage = "/leadfrak [Spieler] [Fraktion]",
        permissionLevel = 75
)
public class LeadFrakCommand extends CommandBase implements TabCompleter {


    public LeadFrakCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 2) {
            showSyntax(player);
            return;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        String frak = args[1];
        player.sendMessage(Prefix.ADMIN + "Du hast §c" + targetplayer.getName() + "§7 in die Fraktion §c" + frak + "§7 gesetzt.");
        targetplayer.sendMessage(Prefix.FACTION + "Du bist nun Leader der Fraktion §c" + frak + "§7!");
        factionManager.setPlayerInFrak(targetplayer, frak, Constants.MAX_FACTION_RANK);
        FactionService factionService = VoidAPI.getService(FactionService.class);
        AdminService adminService = VoidAPI.getService(AdminService.class);
        factionService.setLeader(targetplayer, true);
        adminService.sendMessage(player.getName() + " hat " + targetplayer.getName() + " in die Fraktion " + frak + " gesetzt.", Color.PURPLE);

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
