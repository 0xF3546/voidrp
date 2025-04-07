package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.manager.LocationManager;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.VertragUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RentCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    private final Utils utils;

    public RentCommand(PlayerManager playerManager, LocationManager locationManager, Utils utils) {
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.utils = utils;
        Main.registerCommand("rent", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /rent [Spieler] [Preis]");
            return false;
        }
        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (!targetplayer.isOnline()) player.sendMessage(Prefix.ERROR + "Spieler ist nicht online.");
        Integer haus = locationManager.isPlayerNearOwnHouse(player);
        if (haus == 0) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe deines Hauses.");
            return false;
        }
        if (player.getLocation().distance(targetplayer.getLocation()) > 5) {
            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in deiner nähe.");
            return false;
        }
        try {
            int amount = Integer.parseInt(args[1]);
            if (amount >= 5000 || amount < 1) {
                player.sendMessage(Prefix.ERROR + "Der Betrag muss zwischen 1-5.000$ liegen.");
                return false;
            }
        } catch (Exception ex) {
            player.sendMessage(Prefix.ERROR + "Der Betrag muss numerisch sein.");
            return false;
        }
        House house = Main.getInstance().getHouseManager().getHouse(haus);
        if (house.getRenter().size() >= house.getMieterSlots()) {
            player.sendMessage(Component.text(Prefix.ERROR + "Dein Haus hat nicht genug Mieterslots."));
            return false;
        }
        if (VertragUtil.setVertrag(player, targetplayer, "rental", haus + "_" + args[1])) {
            player.sendMessage("§8[§6Haus§8]§e Du hast " + targetplayer.getName() + " einen Mietvertrag ausgestellt.");
            targetplayer.sendMessage("§6" + player.getName() + " hat dir einen Mietvertrag für Haus " + haus + " in höhe von " + args[1] + "$/PayDay angeboten.");
            utils.vertragUtil.sendInfoMessage(targetplayer);
        } else {
            player.sendMessage(Prefix.ERROR + "§7" + targetplayer.getName() + " hat noch einen Vertrag offen.");
        }
        return false;
    }
}
