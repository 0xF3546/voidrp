package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.agreement.services.VertragUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BeziehungCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public BeziehungCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("beziehung", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /beziehung [Spieler]");
            return false;
        } else {
            if (args[0].equalsIgnoreCase(player.getName())) {
                player.sendMessage(Prefix.ERROR + "Du kannst mit dir selbst keine Beziehung eingehen.");
                return false;
            }
            OfflinePlayer offlinePlayer = Utils.getOfflinePlayer(args[0]);
            if (offlinePlayer == null) {
                player.sendMessage(Prefix.ERROR + args[0] + " wurde nicht gefunden.");
                return false;
            }
            if (!offlinePlayer.isOnline()) {
                player.sendMessage(Prefix.ERROR + offlinePlayer.getName() + " ist nicht online.");
                return false;
            }
        }
        Player player1 = Bukkit.getPlayer(args[0]);
        if (player.getLocation().distance(player1.getLocation()) > 5) {
            player.sendMessage(Prefix.ERROR + player1.getName() + " ist nicht in deiner nähe.");
            return false;
        }
        PlayerData targetplayerData = playerManager.getPlayerData(player1.getUniqueId());
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getRelationShip().isEmpty()) {
            if (targetplayerData.getRelationShip().isEmpty()) {
                VertragUtil.deleteVertrag(player1);
                if (VertragUtil.setVertrag(player, player1, "beziehung", player.getUniqueId().toString())) {
                    player.sendMessage("§6Du hast " + player1.getName() + " nach einer Beziehung gefragt.");
                    player1.sendMessage("§6" + player.getName() + " möchte mit dir zusammen sein.");
                    utils.vertragUtil.sendInfoMessage(player1);
                } else {
                    player.sendMessage(Prefix.ERROR + "Es ist ein Fehler unterlaufen.");
                }
            } else {
                player.sendMessage(Prefix.ERROR + player1.getName() + " ist bereits in einer Beziehung.");
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Du bist bereits in einer Beziehung.");
        }
        return false;
    }
}
