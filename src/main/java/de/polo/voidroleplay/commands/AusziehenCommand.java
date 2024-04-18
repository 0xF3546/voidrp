package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.game.base.housing.Housing;
import de.polo.voidroleplay.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AusziehenCommand implements CommandExecutor {
    private final Utils utils;

    public AusziehenCommand(Utils utils) {
        this.utils = utils;
        Main.registerCommand("ausziehen", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /ausziehen [Haus]");
            return false;
        }
        try {
            int houseNumber = Integer.parseInt(args[0]);
            House houseData = Housing.houseDataMap.get(houseNumber);
            if (houseData == null) {
                player.sendMessage(Main.error + "Dieses Haus wurde nicht gefunden.");
                return false;
            }
            if (houseData.getRenter().get(player.getUniqueId().toString()) == null) {
                player.sendMessage(Main.error + "Du mietest nicht bei Haus " + houseData.getNumber() + ".");
                return false;
            }
            houseData.getRenter().remove(player.getUniqueId().toString());

            utils.housing.updateRenter(houseNumber);
            player.sendMessage("§8[§6Haus§8]§a Du hast den Mietvertrag von Haus " + args[0] + " beendet.");
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(houseData.getOwner());
            if (offlinePlayer.getName() == null) {
                return false;
            }
            if (offlinePlayer.isOnline()) {
                Player player1 = Bukkit.getPlayer(offlinePlayer.getUniqueId());
                assert player1 != null;
                player1.sendMessage("§8[§6Haus§8]§c " + player.getName() + " hat seinen Mietvertrag für Haus " + houseData.getNumber() + " gekündigt!");
            }
        } catch (Exception e) {
            player.sendMessage(Main.error + "Dieses Haus gibt es nicht.");
            e.printStackTrace();
            return false;
        }
        return false;

    }
}
