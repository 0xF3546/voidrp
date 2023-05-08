package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.DataStorage.HouseData;
import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.Utils.Housing;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class unrentCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 2) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(args[0]));
            HouseData houseData = Housing.houseDataMap.get(Integer.parseInt(args[1]));
            if (houseData != null) {
                if (houseData.getOwner().equals(player.getUniqueId().toString())) {
                    if (houseData.getRenter().get(offlinePlayer.getUniqueId().toString()) != null) {
                        houseData.getRenter().remove(offlinePlayer.getUniqueId().toString());
                        Housing.updateRenter(Integer.parseInt(args[1]));
                        player.sendMessage("§8[§6Haus§8]§a Du hast den Mietvertrag von " + offlinePlayer.getName() + " beendet.");
                        if (offlinePlayer.isOnline()) {
                            Player player1 = Bukkit.getPlayer(offlinePlayer.getUniqueId());
                            assert player1 != null;
                            player1.sendMessage("§8[§6Haus§8]§c " + player.getName() + " hat dich aus Haus " + houseData.getNumber() + " rausgeschmissen!");
                        }
                    } else {
                        player.sendMessage(Main.error + offlinePlayer.getName() + " mietet nicht bei dir.");
                    }
                } else {
                    player.sendMessage(Main.error + "Du kannst auf dieses Haus nicht zugreifen.");
                }
            } else {
                player.sendMessage(Main.error + "Dieses Haus wurde nicht gefunden.");
            }
        } else {
            player.sendMessage(Main.error + "Syntax-Fehler: /unrent [Spieler-UUID] [Haus]");
        }
        return false;
    }
}
