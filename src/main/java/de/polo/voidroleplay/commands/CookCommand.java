package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.game.base.CookTimer;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.game.events.MinuteTickEvent;
import de.polo.voidroleplay.utils.ItemManager;
import de.polo.voidroleplay.utils.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CookCommand implements CommandExecutor, Listener {
    private final PlayerManager playerManager;
    private final List<CookTimer> activeCooking = new ArrayList<>();
    public CookCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;

        Main.registerCommand("cook", this);
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) sender;
        CookTimer cookTimer = getCookTimer(player);
        if (cookTimer != null) {
            player.sendMessage(Prefix.MAIN + "Du kochst noch " + cookTimer.getMinutes() + " Minuten.");
            return false;
        }
        House house = Main.getInstance().housing.getNearestHouse(player.getLocation(), 5);
        if (house == null) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe eines Hauses.");
            return false;
        }
        if (!house.getHouseType().isCanCook()) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe eines Wohnwagens!");
            return false;
        }
        if (!Main.getInstance().housing.canPlayerInteract(player, house.getNumber())) {
            player.sendMessage(Prefix.ERROR + "Du hast kein Zugriff auf dieses Haus.");
            return false;
        }
        CookTimer timer = new CookTimer(player, 10, house, player.getLocation());
        activeCooking.add(timer);
        player.sendMessage(Prefix.MAIN + "Du hast das Kochen gestartet.");
        return false;
    }

    private CookTimer getCookTimer(Player player) {
        return activeCooking.stream().filter(c -> c.getPlayer().equals(player)).findFirst().orElse(null);
    }

    @EventHandler
    public void onMinute(MinuteTickEvent event) {
        for (CookTimer timer : activeCooking) {
            if (timer.getPlayer().getLocation().distance(timer.getLocation()) > 15) {
                timer.getPlayer().sendMessage(Prefix.MAIN + "Das kochen wurde beendet.");
                activeCooking.remove(timer);
                continue;
            }
            if (timer.getMinutes() >= 1) {
                timer.setMinutes(timer.getMinutes() - 1);
                continue;
            }
            ItemManager.addCustomItem(timer.getPlayer(), RoleplayItem.CRYSTAL, Main.random(8, 13));
            timer.getPlayer().sendMessage(Prefix.MAIN + "Das kochen wurde beendet.");
            playerManager.addExp(timer.getPlayer(), Main.random(80, 130));
            activeCooking.remove(timer);
        }
    }
}
