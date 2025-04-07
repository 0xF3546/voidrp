package de.polo.voidroleplay.housing.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.game.base.housing.HouseManager;
import de.polo.voidroleplay.utils.Prefix;
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
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /ausziehen [Haus]");
            return false;
        }
        try {
            int houseNumber = Integer.parseInt(args[0]);
            House houseData = HouseManager.houseDataMap.get(houseNumber);
            if (houseData == null) {
                player.sendMessage(Prefix.ERROR + "Dieses Haus wurde nicht gefunden.");
                return false;
            }
            if (houseData.getRenter().get(player.getUniqueId().toString()) == null) {
                player.sendMessage(Prefix.ERROR + "Du mietest nicht bei Haus " + houseData.getNumber() + ".");
                return false;
            }
            houseData.getRenter().remove(player.getUniqueId().toString());

            utils.houseManager.updateRenter(houseNumber);
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
            player.sendMessage(Prefix.ERROR + "Dieses Haus gibt es nicht.");
            e.printStackTrace();
            return false;
        }
        return false;

    }
}
