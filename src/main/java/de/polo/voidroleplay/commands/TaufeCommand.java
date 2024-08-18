package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.FactionManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class TaufeCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    public TaufeCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;

        Main.registerCommand("taufe", this);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (!playerData.getFaction().equalsIgnoreCase("Kirche")) {
            player.sendMessage(Prefix.error_nopermission);
            return false;
        }
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /taufe [Spieler]");
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Prefix.ERROR + args[0] + " ist nicht online.");
            return false;
        }
        if (target == player) {
            player.sendMessage(Prefix.ERROR + "Du kannst dich nicht selbst taufen.");
            return false;
        }
        if (player.getLocation().distance(target.getLocation()) > 5) {
            player.sendMessage(Prefix.ERROR + target.getName() + " ist nicht in der nähe.");
            return false;
        }
        PlayerData targetData = playerManager.getPlayerData(target);
        if (targetData.isBaptized()) {
            player.sendMessage(Prefix.ERROR + target.getName() + " ist bereits getauft.");
            return false;
        }
        targetData.setBaptized(true);
        targetData.save();
        playerManager.addExp(target, Main.random(100, 200));
        playerManager.addExp(player, Main.random(10, 30));
        player.sendMessage(Prefix.MAIN + "Du hast " + targetData.getFirstname() + " " + targetData.getLastname() + " getauft.");
        target.sendMessage(Prefix.MAIN + factionManager.getRankName(playerData.getFaction(), playerData.getFactionGrade()) + " " + playerData.getFirstname() + " " + playerData.getLastname() + " hat dich getauft.");
        return false;
    }
}
