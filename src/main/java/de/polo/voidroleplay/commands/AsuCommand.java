package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.PlayerWanted;
import de.polo.voidroleplay.dataStorage.WantedReason;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AsuCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final Utils utils;
    public AsuCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;

        Main.registerCommand("asu", this);
        Main.addTabCompeter("asu", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.getFaction().equalsIgnoreCase("Polizei") && !playerData.getFaction().equalsIgnoreCase("FBI")) {
            player.sendMessage(Prefix.error_nopermission);
            return false;
        }
        if (!playerData.isDuty()) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht im Dienst.");
            return false;
        }
        if (args.length < 2) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /asu [Spieler] [Grund]");
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Prefix.ERROR + "Der Spieler wurde nicht gefunden.");
            return false;
        }

        String reasonInput = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        WantedReason reason = utils.getStaatUtil().getWantedReason(reasonInput);
        if (reason == null) {
            player.sendMessage(Prefix.ERROR + "Der Grund wurde nicht gefunden.");
            return false;
        }
        PlayerData targetData = playerManager.getPlayerData(target);
        PlayerWanted playerWanted = new PlayerWanted(reason.getId(), player.getUniqueId(), Utils.getTime());
        targetData.setWanted(playerWanted, false).thenAccept(success -> {
            System.out.println("END");
            if (!success) {
                player.sendMessage(Prefix.ERROR + "Der Spieler hat bereits eine höhere Fahndung.");
            } else {
                Main.getInstance().factionManager.sendCustomMessageToFactions("§9HQ: Gesuchter: " + target.getName() + ". Grund: " + reason.getReason(), "Polizei", "FBI");
                Main.getInstance().factionManager.sendCustomMessageToFactions("§9HQ: " + target.getName() + "'s momentanes WantedLevel: " + reason.getWanted(), "Polizei", "FBI");
            }
        });
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> text = new ArrayList<>();
        if (args.length == 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                text.add(player.getName());
            }
        }
        if (args.length != 1) {
            for (WantedReason reason : utils.getStaatUtil().getWantedReasons()) {
                text.add(reason.getReason());
            }
        }
        return text;
    }
}
