package de.polo.core.housing.commands;

import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.agreement.services.VertragUtil;
import de.polo.core.game.base.housing.House;
import de.polo.core.location.services.LocationService;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RentCommand implements CommandExecutor {
    private final PlayerManager playerManager;
    private final Utils utils;

    public RentCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
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
        LocationService locationService = VoidAPI.getService(LocationService.class);
        Integer haus = locationService.isPlayerNearOwnHouse(player);
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
        House house = Main.getHouseManager().getHouse(haus);
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
