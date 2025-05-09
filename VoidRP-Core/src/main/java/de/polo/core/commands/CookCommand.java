package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.game.base.CookTimer;
import de.polo.core.game.base.housing.House;
import de.polo.core.game.events.MinuteTickEvent;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.RoleplayItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class CookCommand implements CommandExecutor, Listener {
    private final PlayerManager playerManager;
    private final List<CookTimer> activeCooking = new ObjectArrayList<>();

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
        House house = Main.houseManager.getNearestHouse(player.getLocation(), 5);
        if (house == null) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe eines Hauses.");
            return false;
        }
        if (!house.getHouseType().isCanCook()) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht in der nähe eines Wohnwagens!");
            return false;
        }
        if (!Main.houseManager.canPlayerInteract(player, house.getNumber())) {
            player.sendMessage(Prefix.ERROR + "Du hast kein Zugriff auf dieses Haus.");
            return false;
        }
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getInventory().getByTypeOrEmpty(RoleplayItem.SCHMERZMITTEL).getAmount() < 10) {
            player.sendMessage(Prefix.ERROR + "Du benötigst 10 Schmerzmittel.");
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
        Iterator<CookTimer> iterator = activeCooking.iterator();

        while (iterator.hasNext()) {
            CookTimer timer = iterator.next();

            if (timer.getPlayer() == null) {
                iterator.remove();
                continue;
            }
            if (timer.getPlayer().getLocation().distance(timer.getLocation()) > 15) {
                timer.getPlayer().sendMessage(Prefix.MAIN + "Das kochen wurde beendet.");
                iterator.remove();
                continue;
            }
            if (timer.getMinutes() >= 1) {
                timer.setMinutes(timer.getMinutes() - 1);
                continue;
            }
            PlayerData playerData = playerManager.getPlayerData(timer.getPlayer());
            if (playerData.getInventory().getByTypeOrEmpty(RoleplayItem.SCHMERZMITTEL).getAmount() < 10) {
                iterator.remove();
                timer.getPlayer().sendMessage(Prefix.ERROR + "Du hast nicht genug Schmerzmittel.");
                continue;
            }
            playerData.getInventory().removeItem(RoleplayItem.SCHMERZMITTEL, 10);
            playerData.getInventory().addItem(RoleplayItem.CRYSTAL, Utils.random(8, 13));
            timer.getPlayer().sendMessage(Prefix.MAIN + "Das kochen wurde beendet.");
            playerManager.addExp(timer.getPlayer(), Utils.random(80, 130));
            iterator.remove();
        }
    }

}
