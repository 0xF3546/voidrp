package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.game.base.housing.House;
import de.polo.core.game.base.housing.HouseManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UnrentCommand implements CommandExecutor {
    private final Utils utils;

    public UnrentCommand(Utils utils) {
        this.utils = utils;
        Main.registerCommand("unrent", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 2) {
            // ISSUE VRP-10000: fixed by adding try-catch block for uuid and player name
            OfflinePlayer offlinePlayer;
            try {
                offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(args[0]));
            } catch (IllegalArgumentException e) {
                offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
            }
            House houseData = HouseManager.houseDataMap.get(Integer.parseInt(args[1]));
            if (houseData != null) {
                if (houseData.getOwner().equals(player.getUniqueId().toString())) {
                    if (houseData.getRenter().get(offlinePlayer.getUniqueId().toString()) != null) {
                        houseData.getRenter().remove(offlinePlayer.getUniqueId().toString());
                        utils.houseManager.updateRenter(Integer.parseInt(args[1]));
                        player.sendMessage("§8[§6Haus§8]§a Du hast den Mietvertrag von " + offlinePlayer.getName() + " beendet.");
                        if (offlinePlayer.isOnline()) {
                            Player player1 = Bukkit.getPlayer(offlinePlayer.getUniqueId());
                            assert player1 != null;
                            player1.sendMessage("§8[§6Haus§8]§c " + player.getName() + " hat dich aus Haus " + houseData.getNumber() + " rausgeschmissen!");
                        }
                    } else {
                        player.sendMessage(Prefix.ERROR + offlinePlayer.getName() + " mietet nicht bei dir.");
                    }
                } else {
                    player.sendMessage(Prefix.ERROR + "Du kannst auf dieses Haus nicht zugreifen.");
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Dieses Haus wurde nicht gefunden.");
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /unrent [Spieler-UUID] [Haus]");
        }
        return false;
    }
}
